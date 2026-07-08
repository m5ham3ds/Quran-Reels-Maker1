const fs = require('fs');
const https = require('https');
const { execSync } = require('child_process');
const path = require('path');

const zipPath = path.join(__dirname, 'repo.zip');
const file = fs.createWriteStream(zipPath);

console.log('Downloading Reelss main branch zip...');
https.get('https://codeload.github.com/m5ham3ds/Reelss/zip/refs/heads/main', (response) => {
  if (response.statusCode !== 200) {
    console.error(`Failed to download: Status ${response.statusCode}`);
    return;
  }
  response.pipe(file);
  file.on('finish', () => {
    file.close();
    console.log('Download finished. Installing decompress tool and extracting...');
    try {
      // Use npx with decompress to extract the zip file
      execSync('npx -y decompress repo.zip .');
      console.log('Extraction success.');
      
      // Let's find the extracted folder and move its contents to current directory
      const files = fs.readdirSync(__dirname);
      const extDir = files.find(f => f.startsWith('Reelss-'));
      if (extDir) {
        console.log(`Found extracted directory: ${extDir}`);
        const fullExtPath = path.join(__dirname, extDir);
        
        const moveDirContents = (src, dest) => {
          if (!fs.existsSync(dest)) {
            fs.mkdirSync(dest, { recursive: true });
          }
          const items = fs.readdirSync(src);
          for (const item of items) {
            const srcPath = path.join(src, item);
            const destPath = path.join(dest, item);
            const stat = fs.statSync(srcPath);
            if (stat.isDirectory()) {
              moveDirContents(srcPath, destPath);
            } else {
              if (item !== 'metadata.json') { // skip overwriting AI Studio's metadata.json with placeholder from repo if we want to be safe, but actually let's see. Let's merge everything
                fs.copyFileSync(srcPath, destPath);
              }
            }
          }
        };
        
        moveDirContents(fullExtPath, __dirname);
        console.log('Successfully copied all files from archive to workspace.');
        
        // Clean up
        fs.rmSync(fullExtPath, { recursive: true, force: true });
        fs.unlinkSync(zipPath);
        console.log('Clean up complete.');
      } else {
        console.error('Extracted directory not found!');
      }
    } catch(err) {
      console.error('Extraction/Move error:', err);
    }
  });
}).on('error', (err) => {
  console.error('Download error:', err);
});
