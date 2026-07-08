const https = require('https');

const postData = JSON.stringify({
  url: 'https://youtube.com/shorts/s34ZVi9fSoI?si=tfvZkdSF2CTQMsPN',
  isAudioOnly: true
});

const req = https.request({
  hostname: 'api.cobalt.tools',
  path: '/api/json',
  method: 'POST',
  headers: {
    'Accept': 'application/json',
    'Content-Type': 'application/json',
    'Content-Length': Buffer.byteLength(postData),
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
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
