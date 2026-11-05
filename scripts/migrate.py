#!/usr/bin/env python3
"""Mechanical import/class migration 1.21.1 -> 26.1.2, driven by NeoForge primers (moves.json)."""
import json, re, sys, os, collections
root = sys.argv[1]
data = json.load(open(os.path.join(os.path.dirname(__file__), 'moves.json')))
VERS = ['1.21.2','1.21.3','1.21.4','1.21.5','1.21.6','1.21.7','1.21.8','1.21.9','1.21.10','1.21.11','26.1']
CLS = re.compile(r'^net(\.[a-z0-9_]+)+\.[A-Z][A-Za-z0-9_]*$')
PKG = re.compile(r'^net(\.[a-z0-9_]+)+$')
cmoves = {v: {} for v in VERS}
pmoves = {v: {} for v in VERS}
for v, a, b in data['moves']:
    if CLS.match(a) and CLS.match(b) and a != b: cmoves[v][a] = b
for v, a, b in data['pkgmoves']:
    if PKG.match(a) and PKG.match(b) and a != b: pmoves[v][a] = b
# classes whose 'rename' in the primers is really a rewrite: leave for manual port
SKIP = {'BakedModel','SimpleBakedModel','ItemOverrides','Explosion'}
for v in VERS:
    for a in list(cmoves[v]):
        if a.rsplit('.',1)[1] in SKIP: del cmoves[v][a]
# manual additions not expressed as bullets in primers
cmoves['1.21.11']['net.minecraft.resources.ResourceLocation'] = 'net.minecraft.resources.Identifier'
pmoves['1.21.11']['net.minecraft.advancements.critereon'] = 'net.minecraft.advancements.criterion'

def move_fqcn(fq):
    for v in VERS:
        if fq in cmoves[v]:
            fq = cmoves[v][fq]; continue
        pkg, cls = fq.rsplit('.', 1)
        if pkg in pmoves[v]:
            fq = pmoves[v][pkg] + '.' + cls
    return fq

imp = re.compile(r'^(import\s+(?:static\s+)?)([\w.]+?)(\.\*)?;', re.M)
changed_files = 0; changed_imports = 0; renames = collections.Counter()
for dp, _, fns in os.walk(root):
    for fn in fns:
        if not fn.endswith('.java'): continue
        p = os.path.join(dp, fn)
        src = open(p, encoding='utf-8').read()
        simple = {}   # old simple -> new simple (for classes imported)
        def sub(m):
            global changed_imports
            head, fq, star = m.group(1), m.group(2), m.group(3) or ''
            if not fq.startswith('net.'): return m.group(0)
            static = 'static' in head
            if star:
                new = fq
                for v in VERS:
                    if fq in pmoves[v]: new = pmoves[v][fq]; fq = new
                if new != m.group(2): changed_imports += 1
                return f'{head}{new}{star};'
            parts = fq.split('.')
            # find class segment index (first uppercase segment)
            idx = next((i for i, s in enumerate(parts) if s[:1].isupper()), None)
            if idx is None: return m.group(0)
            cls_fq = '.'.join(parts[:idx+1]); tail = '.'.join(parts[idx+1:])
            new_cls = move_fqcn(cls_fq)
            if new_cls == cls_fq: return m.group(0)
            changed_imports += 1
            if cls_fq.rsplit('.',1)[1] != new_cls.rsplit('.',1)[1]:
                simple[cls_fq.rsplit('.',1)[1]] = new_cls.rsplit('.',1)[1]
            return f'{head}{new_cls}{("." + tail) if tail else ""}{star};'
        new = imp.sub(sub, src)
        for o, n in simple.items():
            new = re.sub(r'(?<![\w.])' + re.escape(o) + r'(?!\w)', n, new)
            renames[(o, n)] += 1
        # ResourceLocation is used everywhere, including same-file references without import changes
        if re.search(r'(?<![\w.])ResourceLocation(?!\w)', new) and 'Identifier' not in simple.values():
            new = re.sub(r'(?<![\w.])ResourceLocation(?!\w)', 'Identifier', new)
            if 'import net.minecraft.resources.Identifier;' not in new and 'package net.minecraft' not in new:
                pass
        if new != src:
            open(p, 'w', encoding='utf-8').write(new); changed_files += 1
print(f'files changed: {changed_files}, imports changed: {changed_imports}')
for (o, n), c in renames.most_common(40): print(f'  {o} -> {n}: {c} files')
