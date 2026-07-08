from PIL import Image, ImageFilter
import numpy as np

bg_img = Image.open('Pryme-Frontend/src/assets/products/product-4.jpg').convert('RGB')
fg_img = Image.open('Pryme-Frontend/src/assets/products/product-2.jpg').convert('RGB')

bg = np.array(bg_img)
fg = np.array(fg_img)

unique, counts = np.unique(fg.reshape(-1, 3), axis=0, return_counts=True)
sorted_idx = np.argsort(-counts)
fg_bg_color = unique[sorted_idx[0]]

diff = np.linalg.norm(fg - fg_bg_color, axis=2)
mask = diff < 30.0

mask_img = Image.fromarray((mask * 255).astype(np.uint8))
mask_smooth = mask_img.filter(ImageFilter.GaussianBlur(1.0))
mask_smooth = np.array(mask_smooth) / 255.0

out = np.zeros_like(fg, dtype=float)
for c in range(3):
    out[:,:,c] = fg[:,:,c] * (1 - mask_smooth) + bg[:,:,c] * mask_smooth

out = np.clip(out, 0, 255).astype(np.uint8)
out_img = Image.fromarray(out)
out_img.save('test_blend_product_2.png')
print("Blend completed.")
