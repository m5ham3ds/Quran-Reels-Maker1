import gradio_client
client = gradio_client.Client("qalam249/whisperx-frontend")
res = client.predict(
    file_input=None,
    url_input="https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    arabic_text="",
    api_name="/predict"
)
print(res)
