import requests
import json

# ----------------------------
# Step 1: Retrieve Video Details from Elai API (Using a known video URL)
# ----------------------------

# Since you already have the video URL, set it directly.
video_url = "https://app.elai.io/video/67c76f974f8a492abb358380/1223998556343"
print("Using video URL:", video_url)

# ----------------------------
# Step 2: Update a Canvas Page with the Video Link
# ----------------------------

# Replace these values with your Canvas test instance details:
canvas_domain = "northeastern.instructure.com"  # Example: without the "https://"
course_id = "214026"                             # Your Canvas course ID
page_slug = "my-video-page"                      # The page slug (the part after /pages/ in the URL)

# Construct the Canvas API endpoint for updating a page.
canvas_api_url = f"https://{canvas_domain}/api/v1/courses/{course_id}/pages/{page_slug}"

# Instead of embedding with an iframe, we create a clickable link:
embed_code = f'<a href="{video_url}" target="_blank">Click here to watch the video</a>'

# Create the payload to update the Canvas page's content.
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

print("Updating Canvas page with the video link...")
canvas_response = requests.put(canvas_api_url, headers=canvas_headers, json=canvas_payload)

if canvas_response.status_code in [200, 201]:
    print("Canvas page updated successfully!")
    print("Response:", canvas_response.json())
else:
    print("Error updating Canvas page:", canvas_response.status_code)
    print("Response:", canvas_response.text)
