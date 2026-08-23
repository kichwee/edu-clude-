import json
import os
import urllib.request
import re

json_path = r"C:\Users\user\.gemini\antigravity-ide\brain\05100300-080d-4637-85d0-3f1b2bd0642f\.system_generated\steps\33\output.txt"
output_dir = r"C:\Users\user\EDU CLOUDE\stitch_screens"

os.makedirs(output_dir, exist_ok=True)

with open(json_path, 'r', encoding='utf-8') as f:
    data = json.load(f)

for idx, screen in enumerate(data.get('screens', [])):
    title = screen.get('title', f"screen_{idx}")
    # clean title
    safe_title = re.sub(r'[\\/*?:"<>|]', "", title).strip().replace(" ", "_")
    html_code = screen.get('htmlCode', {})
    download_url = html_code.get('downloadUrl')
    
    if download_url:
        print(f"Downloading {title}...")
        try:
            # fetch html
            req = urllib.request.Request(download_url)
            with urllib.request.urlopen(req) as response:
                html = response.read()
            
            # Save to file
            filename = f"{safe_title}_{idx}.html"
            filepath = os.path.join(output_dir, filename)
            with open(filepath, 'wb') as out_f:
                out_f.write(html)
            print(f"Saved to {filepath}")
        except Exception as e:
            print(f"Error downloading {title}: {e}")
    else:
        print(f"Skipping {title}, no htmlCode found.")

print("Export complete.")
