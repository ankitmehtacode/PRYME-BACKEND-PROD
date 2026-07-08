from PIL import Image
import collections

img = Image.open('Pryme-Frontend/src/assets/card-business.png').convert('RGB')
print(f"Dimensions: {img.width}x{img.height}")
colors = img.getcolors(maxcolors=100000)
if colors:
    colors.sort(reverse=True, key=lambda x: x[0])
    print("Top 10 colors:")
    for count, color in colors[:10]:
        print(f"Color: #{color[0]:02x}{color[1]:02x}{color[2]:02x} ({color}), Count: {count}")
else:
    print("Too many colors to count easily.")
