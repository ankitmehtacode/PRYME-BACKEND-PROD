from PIL import Image
import colorsys
import numpy as np

img = Image.open('Pryme-Frontend/src/assets/card-business.png').convert('RGB')
arr = np.array(img) / 255.0

# Convert to HSV
hsv = np.zeros_like(arr)
for i in range(arr.shape[0]):
    for j in range(arr.shape[1]):
        r, g, b = arr[i,j]
        h, s, v = colorsys.rgb_to_hsv(r, g, b)
        
        # Only change greenish pixels
        if 70 < h*360 < 170:
            # Shift hue to ~220 (which is 220/360 = 0.611)
            h = 220.0 / 360.0
            # Scale saturation up slightly
            s = min(1.0, s * 1.35)
            # Scale value down slightly
            v = min(1.0, v * 0.86)
        
        hsv[i,j] = (h, s, v)

# Convert back to RGB
out_arr = np.zeros_like(arr)
for i in range(arr.shape[0]):
    for j in range(arr.shape[1]):
        h, s, v = hsv[i,j]
        r, g, b = colorsys.hsv_to_rgb(h, s, v)
        out_arr[i,j] = (r, g, b)

out_arr = (out_arr * 255).astype(np.uint8)
out_img = Image.fromarray(out_arr)

# Resize to 4K (4096 x 4096)
out_img_4k = out_img.resize((4096, 4096), Image.Resampling.LANCZOS)
out_img_4k.save('test_blue.png')

# Print prominent colors in output
unique_colors, counts = np.unique(out_arr.reshape(-1, 3), axis=0, return_counts=True)
sorted_idx = np.argsort(-counts)
print("Resulting top colors:")
for i in range(5):
    c = unique_colors[sorted_idx[i]]
    print(f"RGB: {c}")

