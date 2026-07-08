from PIL import Image
import numpy as np
from rembg import remove, new_session

# Create clean gradient background at 1024x1024
def get_gradient_bg(size=(1024, 1024)):
    w, h = size
    bg = np.zeros((h, w, 3), dtype=float)
    c_tr = np.array([174, 193, 224])
    c_center = np.array([155, 176, 218])
    c_bl = np.array([145, 169, 217])
    
    for y in range(h):
        for x in range(w):
            dx = (x - w/2) / (w/2)
            dy = (y - h/2) / (h/2)
            pos = (dx - dy) / 2
            
            if pos > 0:
                color = c_center * (1 - pos) + c_tr * pos
            else:
                color = c_center * (1 + pos) + c_bl * (-pos)
                
            bg[y, x] = color
    return Image.fromarray(np.clip(bg, 0, 255).astype(np.uint8)).convert("RGBA")

clean_bg = get_gradient_bg()
session = new_session("u2net")

def process_product(file_name, out_name):
    try:
        fg_img = Image.open(file_name).convert('RGB')
        fg_img = fg_img.resize((1024, 1024))
        
        # Remove background using rembg (u2net model is highly accurate)
        extracted = remove(fg_img, session=session)
        
        # Composite the extracted foreground onto the clean background
        out_img = Image.alpha_composite(clean_bg, extracted)
        
        # Convert back to RGB for final saving (or keep RGBA)
        # 4K resolution
        final_img = out_img.resize((4096, 4096), Image.Resampling.LANCZOS)
        final_img.save(out_name)
        print(f"Saved {out_name}")
    except Exception as e:
        print(f"Error on {file_name}: {e}")

products = {
    'product-2.jpg': 'card-personal-4k.png',
    'product-3.jpg': 'card-home-4k.png',
    'product-1.jpg': 'card-auto-4k.png',
    'product-5.png': 'card-lap-4k.png',
    'product-6.jpg': 'card-balancetransfer-4k.png'
}

for src, dst in products.items():
    process_product(f'Pryme-Frontend/src/assets/products/{src}', f'Pryme-Frontend/src/assets/{dst}')

