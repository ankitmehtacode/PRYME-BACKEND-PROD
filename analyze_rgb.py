from PIL import Image
import numpy as np
from collections import Counter

img = Image.open('Pryme-Frontend/src/assets/card-business.png').convert('RGB')
arr = np.array(img)
unique_colors, counts = np.unique(arr.reshape(-1, 3), axis=0, return_counts=True)
sorted_idx = np.argsort(-counts)
for i in range(10):
    c = unique_colors[sorted_idx[i]]
    count = counts[sorted_idx[i]]
    print(f"RGB: {c}, count: {count}")
