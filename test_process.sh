FILE_PATH=$(curl -s -X POST https://qalam249-whisperx-frontend.hf.space/gradio_api/upload -F 'files=@dummy.wav' | jq -r '.[0]')
echo "File path: $FILE_PATH"
# send predict
curl -s -X POST https://qalam249-whisperx-frontend.hf.space/gradio_api/call/process -H "Content-Type: application/json" -d '{"data": [{"path": "'$FILE_PATH'", "meta": {"_type": "gradio.FileData"}}, "", "text"]}'
