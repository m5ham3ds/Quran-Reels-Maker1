const fs = require('fs');
const path = require('path');

const jarPath = 'gradle/wrapper/gradle-wrapper.jar';
if (fs.existsSync(jarPath)) {
    const stats = fs.statSync(jarPath);
    console.log('Size of gradle-wrapper.jar:', stats.size, 'bytes');
    if (stats.size < 1000) {
        console.log('File is extremely small. Reading content as text:');
        console.log(fs.readFileSync(jarPath, 'utf8'));
    } else {
        const buf = fs.readFileSync(jarPath).slice(0, 4);
        console.log('Header bytes of jar:', buf);
    }
} else {
    console.log('gradle-wrapper.jar does not exist!');
}
