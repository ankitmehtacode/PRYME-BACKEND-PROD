from PIL import Image, ImageFilter
import numpy as np

def transfer_bg(fg_path, bg_path, out_path):
    fg_img = Image.open(fg_path).convert('RGB')
    bg_img = Image.open(bg_path).convert('RGB')
    
    fg = np.array(fg_img)
    bg = np.array(bg_img.resize(fg_img.size))
    
    # Find background color of fg
    unique, counts = np.unique(fg.reshape(-1, 3), axis=0, return_counts=True)
    sorted_idx = np.argsort(-counts)
    fg_bg_color = unique[sorted_idx[0]]
    
    # Create mask for background
    diff = np.linalg.norm(fg - fg_bg_color, axis=2)
    mask = diff < 40.0
    
    mask_img = Image.fromarray((mask * 255).astype(np.uint8))
    mask_smooth = mask_img.filter(ImageFilter.GaussianBlur(2.0))
    mask_smooth = np.array(mask_smooth) / 255.0
    
    # Where mask is 1 (background), we use bg's pixels.
    # Where mask is 0 (center design), we use fg's pixels.
    out = np.zeros_like(fg, dtype=float)
    for c in range(3):
        out[:,:,c] = fg[:,:,c] * (1 - mask_smooth) + bg[:,:,c] * mask_smooth
        
    out = np.clip(out, 0, 255).astype(np.uint8)
    
    # Save test to check
    Image.fromarray(out).save(out_path)

transfer_bg('Pryme-Frontend/src/assets/products/product-1.jpg', 'Pryme-Frontend/src/assets/products/product-4.jpg', 'test_prod1.png')
transfer_bg('Pryme-Frontend/src/assets/products/product-2.jpg', 'Pryme-Frontend/src/assets/products/product-4.jpg', 'test_prod2.png')
print("Test completed.")
