const https = require('https');

const postData = JSON.stringify({
  url: 'https://youtube.com/shorts/s34ZVi9fSoI?si=tfvZkdSF2CTQMsPN',
  audioFormat: 'mp3',
  isAudioOnly: true
});

const req = https.request({
  hostname: 'api.cobalt.tools',
  path: '/',
  method: 'POST',
  headers: {
    'Accept': 'application/json',
    'Content-Type': 'application/json',
    'Origin': 'https://cobalt.tools',
    'Referer': 'https://cobalt.tools/',
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
    'Content-Length': Buffer.byteLength(postData)
  }
}, (res) => {
  let data = '';
  res.on('data', (d) => {
    data += d;
  });
  res.on('end', () => {
    console.log("STATUS: ", res.statusCode);
    console.log("DATA: ", data);
  });
});

req.on('error', (e) => {
  console.error(e);
});

req.write(postData);
req.end();
