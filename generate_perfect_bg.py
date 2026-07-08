from PIL import Image
import colorsys
import numpy as np

img = Image.open('Pryme-Frontend/src/assets/card-business.png').convert('RGB')
arr = np.array(img) / 255.0

# Define the mapping based on analysis
# S_old values
x = [0.176, 0.200, 0.223, 0.239]
# Target S_new and V_new values
y_s = [0.223, 0.289, 0.288, 0.331]
y_v = [0.878, 0.866, 0.854, 0.850]

hsv = np.zeros_like(arr)
for i in range(arr.shape[0]):
    for j in range(arr.shape[1]):
        r, g, b = arr[i,j]
        h, s, v = colorsys.rgb_to_hsv(r, g, b)
        
        # Only change greenish pixels
        if 70 < h*360 < 170:
            h = 219.0 / 360.0
            
            # Map S and V based on original S
            new_s = np.interp(s, x, y_s)
            new_v = np.interp(s, x, y_v)
            
            # If the pixel was darker/different in original V, adjust new_v accordingly
            # But since V is mostly 1.0, we just multiply by v to keep any shadows
            new_v = new_v * v
            
            s = new_s
            v = new_v
            
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

# Save for each product
products = ['personal', 'home', 'auto', 'lap', 'balancetransfer']
for p in products:
    out_img_4k.save(f'Pryme-Frontend/src/assets/card-{p}-4k.png')

# Print prominent colors in output to verify
unique_colors, counts = np.unique(out_arr.reshape(-1, 3), axis=0, return_counts=True)
sorted_idx = np.argsort(-counts)
print("Resulting top colors:")
for i in range(10):
    c = unique_colors[sorted_idx[i]]
    print(f"RGB: {c}")

