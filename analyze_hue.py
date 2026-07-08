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
        hsv[i,j] = colorsys.rgb_to_hsv(r, g, b)

hues = hsv[:,:,0] * 360
sats = hsv[:,:,1]
vals = hsv[:,:,2]

print("Hue percentiles:", np.percentile(hues, [0, 25, 50, 75, 100]))
print("Sat percentiles:", np.percentile(sats, [0, 25, 50, 75, 100]))
print("Val percentiles:", np.percentile(vals, [0, 25, 50, 75, 100]))

# Check if there are non-green hues (e.g. white, black, or other colors)
non_green = np.sum((hues < 70) | (hues > 150))
total = arr.shape[0] * arr.shape[1]
print(f"Non-green pixels: {non_green} / {total} ({non_green/total*100:.2f}%)")

white_or_black = np.sum((sats < 0.05) | (vals < 0.1))
print(f"White/Black pixels: {white_or_black} / {total} ({white_or_black/total*100:.2f}%)")

