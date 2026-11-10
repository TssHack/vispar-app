"""Renders the Vispar liquid-glass bolt: launcher icons and the in-app logo.

Usage: pip install numpy scipy pillow && python3 tools/logo/render.py   (writes tools/logo/out/)
"""
import numpy as np
from PIL import Image, ImageFilter
from scipy import ndimage as ndi

import os
HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(HERE, 'bolt_mask.png')
OUT = os.path.join(HERE, 'out')
os.makedirs(OUT, exist_ok=True)
S = 2048  # supersampled canvas

def bolt_mask(size, scale):
    """Bolt silhouette from the old logo alpha, centred, scaled to `scale` of the canvas."""
    a = Image.open(SRC).convert('L')
    bbox = a.getbbox()
    a = a.crop(bbox)
    w, h = a.size
    target = int(size * scale)
    k = target / max(w, h)
    a = a.resize((max(1, int(w * k)), max(1, int(h * k))), Image.LANCZOS)
    canvas = Image.new('L', (size, size), 0)
    canvas.paste(a, ((size - a.size[0]) // 2, (size - a.size[1]) // 2))
    m = (np.asarray(canvas, np.float32) > 12).astype(np.float32)
    m = (ndi.gaussian_filter(m, size * 0.006) > 0.5).astype(np.float32)
    return m

def smoothstep(e0, e1, x):
    t = np.clip((x - e0) / (e1 - e0), 0, 1)
    return t * t * (3 - 2 * t)

def normalize(v):
    return v / np.maximum(np.linalg.norm(v, axis=-1, keepdims=True), 1e-6)

def glass(mask, background, opaque_bg=True):
    """Shade a thick liquid-glass body over `background` (HxWx3, 0..1)."""
    H, W = mask.shape
    inside = mask > 0.5
    d = ndi.distance_transform_edt(inside).astype(np.float32)
    d = ndi.gaussian_filter(d, 0.012 * W) * inside
    bevel = 0.075 * W
    t = np.clip(d / bevel, 0, 1)
    height = np.sqrt(1 - (1 - t) ** 2)             # rounded rim, flat top
    height = ndi.gaussian_filter(height, 0.006 * W)
    gy, gx = np.gradient(height * bevel * 0.9)
    n = normalize(np.dstack([-gx, -gy, np.ones_like(gx)]))
    nx, ny, nz = n[..., 0], n[..., 1], n[..., 2]

    yy, xx = np.mgrid[0:H, 0:W].astype(np.float32)

    # Refraction with dispersion: each channel bends a little differently.
    out = np.zeros((H, W, 3), np.float32)
    strength = 0.10 * W
    for c, k in enumerate((1.06, 1.0, 0.94)):
        sx = np.clip(xx - nx * strength * k, 0, W - 1)
        sy = np.clip(yy - ny * strength * k, 0, H - 1)
        out[..., c] = ndi.map_coordinates(background[..., c], [sy, sx], order=1)
    # Glass slightly magnifies and brightens what's behind (lens), tinted cool white.
    body = out * 0.82 + 0.06
    body *= np.array([0.97, 0.99, 1.03])

    # Environment reflection: a big soft box up-left and a floor bounce.
    view = np.array([0, 0, 1.0])
    r = 2 * nz[..., None] * n - view                 # reflect(-view)
    rx, ry, rz = r[..., 0], r[..., 1], r[..., 2]
    softbox = smoothstep(0.25, 0.85, (-ry * 0.8 - rx * 0.6)) * smoothstep(-0.2, 0.4, rz)
    bounce = smoothstep(0.3, 0.9, ry) * 0.35
    env = softbox * 1.0 + bounce
    fresnel = 0.04 + 0.96 * (1 - nz) ** 4
    refl = env[..., None] * (0.25 + 0.75 * fresnel[..., None])

    # Sharp specular highlights (two lights).
    def spec(lx, ly, lz, p):
        l = normalize(np.array([lx, ly, lz], np.float32))
        h = normalize(l + view)
        return np.clip((n @ h), 0, 1) ** p
    highlight = spec(-0.55, -0.7, 0.45, 90) * 1.2 + spec(0.6, 0.55, 0.55, 140) * 0.45

    # Bright thin rim where the surface turns away, and an inner caustic glow along the lower edge.
    rim = smoothstep(0.55, 1.0, 1 - nz) * 0.35
    rim_line = np.exp(-((d - 2.5) / 2.2) ** 2) * 0.55
    caustic = np.clip(ndi.gaussian_filter((ny > 0.15).astype(np.float32) * (1 - nz), 18) * 1.6, 0, 1) * 0.35

    if not opaque_bg:
        # Clear glass on an unknown background: white light layer over a faint smoky body.
        light = np.clip(0.15 + refl.mean(-1) * 1.25 + highlight * 1.2 + rim * 1.3 + rim_line + caustic * 1.3, 0, 1)
        shade = np.clip(0.10 + 0.45 * smoothstep(0.0, 0.9, (nx * 0.6 + ny * 0.8)) * (1 - nz) * 2.2, 0, 0.6)
        aa = ndi.gaussian_filter(inside.astype(np.float32), 1.2)
        alpha = np.clip(light + shade * (1 - light), 0, 1) * aa
        premul = np.dstack([light] * 3) * aa[..., None]
        colr = premul / np.maximum(alpha[..., None], 1e-4)
        return np.clip(colr, 0, 1), alpha
    col = body + refl + highlight[..., None] + rim[..., None] + rim_line[..., None] + caustic[..., None]
    col = np.clip(col, 0, 1)

    aa = ndi.gaussian_filter(inside.astype(np.float32), 1.2)
    if opaque_bg:
        return background * (1 - aa[..., None]) + col * aa[..., None], aa
    return col, aa

def studio_background(H, W, dark=True):
    yy, xx = np.mgrid[0:H, 0:W].astype(np.float32) / H
    base = np.full((H, W, 3), 0.015, np.float32)
    glow1 = np.exp(-(((xx - 0.30) ** 2 + (yy - 0.22) ** 2) / 0.08))[..., None] * np.array([0.30, 0.30, 0.32])
    glow2 = np.exp(-(((xx - 0.78) ** 2 + (yy - 0.85) ** 2) / 0.05))[..., None] * np.array([0.16, 0.17, 0.20])
    # Faint diagonal light bands so the refraction has something to bend.
    bands = (0.5 + 0.5 * np.sin((xx * 1.0 + yy * 0.6) * 9)) ** 8 * 0.06
    r = np.sqrt((xx - 0.5) ** 2 + (yy - 0.5) ** 2)
    ring = np.exp(-((r - 0.27) / 0.035) ** 2)[..., None] * np.array([0.30, 0.31, 0.34])
    halo = np.exp(-(r / 0.30) ** 2)[..., None] * np.array([0.10, 0.10, 0.11])
    return np.clip(base + glow1 + glow2 + bands[..., None] + halo * 1.4, 0, 1)

def drop_shadow(bg, mask, offset=(0.02, 0.035), blur=0.035, opacity=0.65):
    H, W = mask.shape
    sh = ndi.shift(mask, (offset[1] * H, offset[0] * W), order=1)
    sh = ndi.gaussian_filter(sh, blur * W) * opacity
    return bg * (1 - sh[..., None])

def caustic_light(bg, mask, offset=(0.025, 0.05), opacity=0.25):
    H, W = mask.shape
    m = ndi.shift(mask, (offset[1] * H, offset[0] * W), order=1)
    inner = ndi.gaussian_filter(np.clip(m - ndi.gaussian_filter(m, 0.03 * W), 0, 1), 0.012 * W)
    return np.clip(bg + inner[..., None] * opacity * 3, 0, 1)

def save(arr, path, size, alpha=None):
    img = Image.fromarray((np.clip(arr, 0, 1) * 255 + 0.5).astype(np.uint8), 'RGB')
    if alpha is not None:
        a = Image.fromarray((np.clip(alpha, 0, 1) * 255 + 0.5).astype(np.uint8), 'L')
        img.putalpha(a)
    img = img.resize((size, size), Image.LANCZOS)
    img.save(path)

# 1) Full icon (opaque): bolt on a black studio backdrop.
def icon(scale, path, size):
    m = bolt_mask(S, scale)
    bg = studio_background(S, S)
    bg = drop_shadow(bg, m)
    bg = caustic_light(bg, m)
    out, _ = glass(m, bg)
    save(out, path, size)

# 2) Transparent logo for in-app use: glass body over a neutral, alpha from coverage + glass density.
def transparent(path, size):
    m = bolt_mask(S, 0.86)
    bg = studio_background(S, S) * 0.6 + 0.25
    col, alpha = glass(m, bg, opaque_bg=False)
    save(col, path, size, alpha)

import sys
icon(0.62, OUT + '/icon_full.png', 1024)
icon(0.44, OUT + '/icon_adaptive_fg.png', 1024)
transparent(OUT + '/logo_glass.png', 768)
print('ok')
