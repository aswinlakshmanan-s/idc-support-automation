import requests
import json
import time

# ----------------------------
# Step 1: Generate Video via Elai API
# ----------------------------

elai_url = "https://apis.elai.io/api/v1/videos"
# Using a payload that includes a slide with content.
elai_payload = {
    "name": "Hello from API!",
    "tags": ["test"],
    "public": False,
    "slides": [
        {
            "id": 1,
            "canvas": {
                "objects": [
                    {
                        "type": "avatar",
                        "left": 151.5,
                        "top": 36,
                        "fill": "#4868FF",
                        "scaleX": 0.3,
                        "scaleY": 0.3,
                        "width": 1080,
                        "height": 1080,
                        "src": "https://elai-avatars.s3.us-east-2.amazonaws.com/common/gia/casual/gia_casual.png",
                        "avatarType": "transparent",
                        "animation": {
                            "type": None,
                            "exitType": None
                        }
                    }
                ],
                "background": "#ffffff",
                "version": "4.4.0"
            },
            "avatar": {
                "code": "gia.casual",
                "gender": "female",
                "canvas": "https://elai-avatars.s3.us-east-2.amazonaws.com/common/gia/casual/gia_casual.png"
            },
            "animation": "fade_in",
            "language": "English",
            "speech": "Hi there! It's my first video created by Elai API.",
            "voice": "en-US-AriaNeural",
            "voiceType": "text",
            "voiceProvider": "azure"
        }
    ]
}

elai_headers = {
    "accept": "application/json",
    "content-type": "application/json",
    "Authorization": "Bearer EWidRSjNTr8ItdgP2Xe59w5POAwatWFF"  # Replace with your actual Elai API token
}

print("Creating video via Elai API...")
elai_response = requests.post(elai_url, json=elai_payload, headers=elai_headers)
if elai_response.status_code != 200:
    print("Error creating video in Elai:", elai_response.text)
    exit(1)

elai_data = elai_response.json()
print("Initial Elai API response:")
print(json.dumps(elai_data, indent=4))

# Get the video ID (could be under "id" or "_id")
video_id = elai_data.get("_id") or elai_data.get("id")
if not video_id:
    print("No video ID found in the response!")
    exit(1)

print("Video created. Video ID:", video_id)

# ----------------------------
# Step 2: Poll the Elai API Until the Video is Ready
# ----------------------------

# Construct the polling URL based on the video ID.
poll_url = f"https://apis.elai.io/api/v1/videos/{video_id}"

max_attempts = 10   # Adjust as needed
attempt = 0
video_url = None

print("Polling for video completion...")
while attempt < max_attempts:
    poll_response = requests.get(poll_url, headers=elai_headers)
    if poll_response.status_code == 200:
        poll_data = poll_response.json()
        status = poll_data.get("status")
        print(f"Attempt {attempt+1}: Video status is '{status}'")
        # Check if the video is published and the URL is available
        if status == "published" and poll_data.get("video_url"):
            video_url = poll_data["video_url"]
            break
    else:
        print("Error polling video status:", poll_response.text)
    
    attempt += 1
    time.sleep(60)  # Wait 60 seconds before polling again

if not video_url:
    print("Video did not become ready after polling.")
    exit(1)

print("Video is ready! Video URL:", video_url)

# ----------------------------
# Step 3: Update a Canvas Page with the Generated Video
# ----------------------------

# Replace these with your Canvas test instance details:
canvas_domain = "northeastern.instructure.com"  # Do not include "https://" in this variable
course_id = "1234"                              # Your Canvas course ID (as a string)
page_slug = "my-video-page"                       # The slug/URL identifier for the page

# Construct the Canvas API endpoint for updating a page.
canvas_api_url = f"https://{canvas_domain}/api/v1/courses/{course_id}/pages/{page_slug}"

# Prepare the embed code for the video using an iframe.
embed_code = f'<iframe src="{video_url}" width="640" height="360" frameborder="0" allowfullscreen></iframe>'

# Create the payload to update the Canvas page.
canvas_payload = {
    "wiki_page": {
        "body": f"<p>Check out our new video:</p>{embed_code}"
    }
}

# Set up Canvas API headers (replace with your actual Canvas API token).
canvas_headers = {
    "Authorization": "Bearer 14523~A8zR92Tz7mvX4yF4fyJnNCTJtZwaWMNCvfGY9Gc4wuEAQvkknZ6zchZZfN3veeTH",  # Replace with your Canvas API token
    "Content-Type": "application/json"
}

print("Updating Canvas page...")
canvas_response = requests.put(canvas_api_url, headers=canvas_headers, json=canvas_payload)
if canvas_response.status_code in [200, 201]:
    print("Canvas page updated successfully!")
    print("Response:", canvas_response.json())
else:
    print("Error updating Canvas page:", canvas_response.status_code)
    print("Response:", canvas_response.text)
