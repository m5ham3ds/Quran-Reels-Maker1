import requests
import json
import time

url_input = "https://www.youtube.com/watch?v=dQw4w9WgXcQ" # something short
base_url = "https://qalam249-whisperx-frontend.hf.space/gradio_api/call/process"

# POST to /call/process
res = requests.post(base_url, json={
    "data": [
        None,
        url_input,
        ""
    ]
})
event_id = res.json()["event_id"]
print("Event ID:", event_id)

stream_url = f"https://qalam249-whisperx-frontend.hf.space/gradio_api/call/process/{event_id}"
res = requests.get(stream_url, stream=True)
for line in res.iter_lines():
    if line:
        print(line.decode('utf-8'))
