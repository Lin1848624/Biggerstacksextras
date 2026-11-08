"""Bigger Stacks Extras 图标生成器（400x400，CurseForge 用）。
设计：深色底 + 四层金色堆叠板 + 青色向上箭头；缩到 64px 仍能看出“一叠东西”。
"""
from PIL import Image, ImageDraw, ImageFilter, ImageFont
import os

OUT = "/home/hermes/Project/MC_mods/biggerstacksextras/dist"
S = 1200
TOP_COLOR = (13, 18, 27)
BOTTOM_COLOR = (3, 5, 9)
GOLD_TOP = (248, 201, 104)
GOLD_BOT = (168, 96, 26)
GOLD_EDGE = (120, 66, 18)
TEAL = (64, 210, 196)
TEXT_GRAY = (146, 166, 186)

os.makedirs(OUT, exist_ok=True)


def font(size):
    for path in (
        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
        "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf",
    ):
        if os.path.exists(path):
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()


# 背景渐变
bg = Image.new("RGB", (S, S))
for y in range(S):
    t = y / (S - 1)
    t = t * t * (3 - 2 * t)
    c = tuple(int(TOP_COLOR[i] * (1 - t) + BOTTOM_COLOR[i] * t) for i in range(3))
    ImageDraw.Draw(bg).line([(0, y), (S, y)], fill=c)

# 斜网格
grid = Image.new("RGBA", (S, S), (0, 0, 0, 0))
gd = ImageDraw.Draw(grid)
for x in range(-S, S * 2, 64):
    gd.line([(x, 0), (x - S, S)], fill=(70, 110, 130, 16), width=2)
for x in range(0, S * 2, 64):
    gd.line([(x, 0), (x + S, S)], fill=(70, 110, 130, 10), width=2)
img = Image.alpha_composite(bg.convert("RGBA"), grid)
d = ImageDraw.Draw(img)

# 外框
d.rounded_rectangle([54, 54, S - 54, S - 54], radius=92, outline=(43, 57, 74, 255), width=6)
d.rounded_rectangle([72, 72, S - 72, S - 72], radius=76, outline=(224, 168, 74, 80), width=2)

# 背后光晕
glow = Image.new("RGBA", (S, S), (0, 0, 0, 0))
ImageDraw.Draw(glow).ellipse([260, 300, 940, 980], fill=(226, 140, 44, 90))
glow = glow.filter(ImageFilter.GaussianBlur(150))
img = Image.alpha_composite(img, glow)
d = ImageDraw.Draw(img)

# 四层堆叠板
BAR_W = 640
BAR_H = 120
GAP = 28
offsets = [-16, 14, -10, 12]
for i in range(4):
    y = 340 + i * (BAR_H + GAP)
    x0 = (S - BAR_W) // 2 + offsets[i]
    x1 = x0 + BAR_W

    shadow = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    ImageDraw.Draw(shadow).rounded_rectangle(
        [x0 + 10, y + 18, x1 + 10, y + BAR_H + 18], radius=34, fill=(0, 0, 0, 150)
    )
    shadow = shadow.filter(ImageFilter.GaussianBlur(18))
    img = Image.alpha_composite(img, shadow)
    d = ImageDraw.Draw(img)

    bar = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    mask = Image.new("L", (S, S), 0)
    ImageDraw.Draw(mask).rounded_rectangle([x0, y, x1, y + BAR_H], radius=34, fill=255)
    grad = Image.new("RGBA", (S, S))
    for yy in range(y, y + BAR_H + 1):
        t = (yy - y) / BAR_H
        t = t * t * (3 - 2 * t)
        c = tuple(int(GOLD_TOP[k] * (1 - t) + GOLD_BOT[k] * t) for k in range(3)) + (255,)
        ImageDraw.Draw(grad).line([(x0, yy), (x1, yy)], fill=c)
    bar.paste(grad, (0, 0), mask)
    img = Image.alpha_composite(img, bar)
    d = ImageDraw.Draw(img)

    d.rounded_rectangle([x0, y, x1, y + BAR_H], radius=34, outline=GOLD_EDGE + (255,), width=5)
    d.line([(x0 + 40, y + 14), (x1 - 40, y + 14)], fill=(255, 236, 190, 110), width=6)

# 右侧向上箭头
ax = 1000
d.line([(ax, 870), (ax, 360)], fill=TEAL + (235,), width=26)
d.polygon([(ax, 300), (ax - 66, 396), (ax + 66, 396)], fill=TEAL + (235,))

# 文字
ft = font(46)
tw = d.textlength("BIGGER", font=ft) + 7 * 5
xx = (S - tw) / 2
for ch in "BIGGER":
    d.text((xx, 172), ch, font=ft, fill=TEXT_GRAY + (255,))
    xx += d.textlength(ch, font=ft) + 7

fb = font(66)
tw = d.textlength("STACKS", font=fb) + 8 * 5
xx = (S - tw) / 2
for ch in "STACKS":
    d.text((xx, 966), ch, font=fb, fill=GOLD_TOP + (255,))
    xx += d.textlength(ch, font=fb) + 8

icon = img.convert("RGB").resize((400, 400), Image.Resampling.LANCZOS)
icon.save(os.path.join(OUT, "icon-400.png"), "PNG", optimize=True)
icon.save(os.path.join(OUT, "icon.png"), "PNG", optimize=True)
icon.resize((1024, 1024), Image.Resampling.LANCZOS).save(os.path.join(OUT, "icon-1024.png"), "PNG", optimize=True)
icon.resize((64, 64), Image.Resampling.LANCZOS).save(os.path.join(OUT, "icon-64.png"), "PNG", optimize=True)
print("written:", OUT)
