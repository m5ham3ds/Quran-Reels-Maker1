RESPONSE=$(curl -s -X POST https://qalam249-whisperx-frontend.hf.space/gradio_api/call/process -H "Content-Type: application/json" -d '{"data": [null, "https://www.youtube.com/watch?v=M57Fi19_Wls", ""]}')
echo "Response: $RESPONSE"
EVENT_ID=$(echo $RESPONSE | grep -o '"event_id":"[^"]*' | cut -d'"' -f4)
echo "Event ID: $EVENT_ID"
curl -N "https://qalam249-whisperx-frontend.hf.space/gradio_api/call/process/$EVENT_ID"
