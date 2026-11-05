#!/usr/bin/env python3
"""build.log -> errors.txt (one line per error incl. missing symbol) + summary.txt"""
import re, sys, collections
log = open(sys.argv[1], encoding='utf-8', errors='replace').read().splitlines()
errs = []
i = 0
while i < len(log):
    m = re.match(r'^(?:.*?/ie/)?(src/\S+?\.java):(\d+): error: (.*)$', log[i])
    if m:
        f, n, msg = m.group(1), m.group(2), m.group(3)
        src = log[i+1].strip() if i+1 < len(log) else ''
        sym = loc = ''
        j = i + 2
        while j < len(log) and j < i + 12 and not re.match(r'^(?:.*?/ie/)?src/\S+?\.java:\d+: (error|warning)', log[j]):
            t = log[j].strip()
            if t.startswith('symbol:'): sym = t[7:].strip()
            elif t.startswith('location:'): loc = t[9:].strip()
            elif t.startswith(('required:', 'found:', 'reason:')): msg += ' | ' + t
            j += 1
        errs.append((f, n, msg, sym, loc, src))
        i = j
    else:
        i += 1
with open('errors.txt', 'w', encoding='utf-8') as o:
    for f, n, msg, sym, loc, src in errs:
        o.write(f'{f}:{n}: {msg}' + (f' || symbol: {sym}' if sym else '') + (f' || in: {loc}' if loc else '') + f' || code: {src[:140]}\n')
sy = collections.Counter(s for _, _, _, s, _, _ in errs if s)
kinds = collections.Counter(re.sub(r'\s*\|.*', '', m) for _, _, m, _, _, _ in errs)
files = collections.Counter(f for f, *_ in errs)
with open('summary.txt', 'w', encoding='utf-8') as o:
    o.write(f'errors: {len(errs)}\n\n== by kind ==\n')
    for k, v in kinds.most_common(40): o.write(f'{v:5} {k}\n')
    o.write('\n== top missing symbols ==\n')
    for k, v in sy.most_common(150): o.write(f'{v:5} {k}\n')
    o.write('\n== top files ==\n')
    for k, v in files.most_common(60): o.write(f'{v:5} {k}\n')
print(open('summary.txt').read()[:3000])
