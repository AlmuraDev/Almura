#!/usr/bin/env python3
"""
Makes Almura's 4.0 content: a copy of the 3.0 content (the 1.12 content loader's files) with its mistakes fixed and its OBJ models in
the conventions NeoForge 26.3 reads, so the 4.0 loader reads it as it is instead of repairing it.

    python fix_content.py --source "H:/Almura 3.0/ContentLoader/3.0/fs_2" --out runs/content-4.0

--source is the folder holding assets/ (it is only read). --out must not exist or be empty (or pass --replace): the copy is made there
and fixed, and FIXES.md there lists every change and what still needs a person to decide.

What it fixes (see FIXES.md for each file):
  1. File names 26.3 can't load (capitals, Thumbs.db).
  2. OBJ models: "mtllib" names the material library by its full path (NeoForge looks from the namespace's root, 1.12's Forge from
     beside the model); texture coordinates' V is flipped (1.12's Forge flipped it when reading); the libraries' textures are named by
     their full id (almura:block/x, where 1.12's Forge looked beside the model and in block/).
  3. JSON block models: elements given with "from" past "to" put the right way round; a model drawing its own elements with no
     particle texture given its first texture as one.
  4. Drops naming an item that doesn't exist, where it is a misspelling of the block itself (cactus_greensnl for cactus_greensml), or
     the letters' normal/letter/X for horizontal/letter/X: the block itself.
  5. Translation keys with capitals lowered; missing names added.
  6. Potion effect ids with capitals lowered (minecraft:STRENGTH).
  7. Wild crops in the grass mix named at an age that isn't their ripe one: their ripe one.
  8. Crop hydration asking farmland for "level" (farmland's is "moisture"; 1.12 ignored the unknown property, so any farmland
     watered the crop): the property removed, keeping the behaviour players had.

It changes text in place where it can, so the files keep their comments and layout.
"""

import argparse
import difflib
import json
import os
import re
import shutil
import sys
from pathlib import Path

VALID_PATH = re.compile(r'^[a-z0-9/._-]+$')


class Fixes:
    def __init__(self):
        self.done = {}      # section -> [lines]
        self.decide = []    # what needs a person

    def add(self, section, line):
        self.done.setdefault(section, []).append(line)

    def decision(self, line):
        if line not in self.decide:
            self.decide.append(line)

    def write(self, path, source):
        with open(path, 'w', encoding='utf-8') as out:
            out.write('# Almura 4.0 content: fixes\n\n')
            out.write(f'Made by `tools/content-4.0/fix_content.py` from `{source}`.\n\n')
            if self.decide:
                out.write('## Needs a decision\n\n')
                for line in self.decide:
                    out.write(f'- {line}\n')
                out.write('\n')
            for section, lines in self.done.items():
                out.write(f'## {section} ({len(lines)})\n\n')
                for line in sorted(lines):
                    out.write(f'- {line}\n')
                out.write('\n')


def lenient_json(text):
    """1.12's content JSON: // comments and trailing commas allowed."""
    text = re.sub(r'(?<![:"\\])//[^\n]*', '', text)
    text = re.sub(r',(\s*[}\]])', r'\1', text)
    return json.loads(text)


def read(path):
    return path.read_text(encoding='utf-8')


def write(path, text):
    path.write_text(text, encoding='utf-8', newline='\n')


class Content:
    """The content copy: assets/<namespace>/{content,models,textures,blockstates}."""

    def __init__(self, root):
        self.root = root
        self.assets = root / 'assets'

    def namespaces(self):
        return sorted(p.name for p in self.assets.iterdir() if p.is_dir())

    def ns(self, namespace):
        return self.assets / namespace

    def content_files(self, namespace, kind):
        base = self.ns(namespace) / 'content' / kind
        if not base.is_dir():
            return []
        files = []
        for path in sorted(base.rglob('*.json')):
            rel = path.relative_to(base).as_posix()
            if any(part.startswith('_') for part in rel.split('/')):
                continue
            files.append((f'{namespace}:{rel[:-5]}', path))
        return files

    def ids(self):
        """Every content item and block id (an item and a block are named alike)."""
        ids = set()
        for namespace in self.namespaces():
            for kind in ('item', 'block'):
                ids.update(identifier for identifier, _ in self.content_files(namespace, kind))
        return ids


# 1. File names --------------------------------------------------------------------------------------------------------------------

def fix_file_names(content, fixes):
    for namespace in content.namespaces():
        base = content.ns(namespace)
        for path in sorted(base.rglob('*')):
            if not path.is_file():
                continue
            rel = path.relative_to(base).as_posix()
            if VALID_PATH.match(rel):
                continue
            if path.name.lower() == 'thumbs.db':
                path.unlink()
                fixes.add('File names 26.3 can\'t load', f'`{namespace}/{rel}`: deleted (Windows\' thumbnail cache)')
                continue
            fixed = re.sub(r'[^a-z0-9/._-]', '_', rel.lower())
            target = base / fixed
            # Windows doesn't tell Untitled-1.png from untitled-1.png: the same file isn't "another file there"
            if target.exists() and not target.samefile(path):
                fixes.decision(f'`{namespace}/{rel}` can\'t be renamed to `{fixed}`: that file exists')
                continue
            temporary = path.with_name(path.name + '.renaming')
            path.rename(temporary)
            temporary.rename(target)
            fixes.add('File names 26.3 can\'t load', f'`{namespace}/{rel}` renamed `{fixed}`')


# 2. OBJ models --------------------------------------------------------------------------------------------------------------------

def material_library(models, obj_rel, folder):
    """The OBJ's material library as 1.12's Forge found it (see ContentPackBuilder before 4.0's content): beside the model, under
    block/, or under models/; failing those the model's own name with .mtl. Its path under models/, or None."""
    named = None
    for line in read(models / obj_rel).splitlines():
        line = line.strip()
        if line.startswith('mtllib '):
            named = line[len('mtllib '):].strip()
            break
    candidates = []
    if named is not None:
        path = named.split(':', 1)[1] if ':' in named else named
        path = path.lstrip('/')
        name = path.rsplit('/', 1)[-1]
        candidates += [folder + name, 'block/' + path, path]
    candidates.append(obj_rel[:-4] + '.mtl')
    for candidate in candidates:
        if (models / candidate).is_file():
            return candidate
    return None


def texture_of(content, model_namespace, model_folder, ref):
    """A material library's texture (almura:x.png) as 1.12's Forge found it: beside the model, in block/, or as named. Its id, or None."""
    namespace, path = (ref.split(':', 1) if ':' in ref else (model_namespace, ref))
    if path.endswith('.png'):
        path = path[:-4]
    textures = content.ns(namespace) / 'textures'
    candidates = [path, model_folder + path] if '/' in path else [model_folder + path, 'block/' + path, path]
    for candidate in candidates:
        if (textures / (candidate + '.png')).is_file():
            return f'{namespace}:{candidate}'
    return None


def flip_v(line):
    parts = line.split()
    if len(parts) < 3:
        return line
    try:
        v = float(parts[2])
    except ValueError:
        return line
    flipped = 1.0 - v
    text = f'{flipped:.8f}'.rstrip('0').rstrip('.')
    if text in ('-0', ''):
        text = '0'
    parts[2] = text
    return ' '.join(parts)


def fix_objs(content, fixes):
    done_mtls = set()
    for namespace in content.namespaces():
        models = content.ns(namespace) / 'models'
        if not models.is_dir():
            continue
        for obj in sorted(models.rglob('*.obj')):
            obj_rel = obj.relative_to(models).as_posix()
            folder = obj_rel.rsplit('/', 1)[0] + '/' if '/' in obj_rel else ''
            mtl = material_library(models, obj_rel, folder)
            lines = read(obj).splitlines()
            out, flipped, had_mtllib = [], 0, False
            for line in lines:
                stripped = line.strip()
                if stripped.startswith('mtllib '):
                    had_mtllib = True
                    if mtl is not None:
                        line = f'mtllib {namespace}:models/{mtl}'
                elif stripped.startswith('vt '):
                    line = flip_v(stripped)
                    flipped += 1
                out.append(line)
            if mtl is not None and not had_mtllib:
                out.insert(0, f'mtllib {namespace}:models/{mtl}')
            write(obj, '\n'.join(out) + '\n')
            fixes.add('OBJ models', f'`{namespace}:models/{obj_rel}`: material library `{namespace}:models/{mtl}`, {flipped} texture '
                      'coordinates flipped' if mtl else f'`{namespace}:models/{obj_rel}`: {flipped} texture coordinates flipped')
            if mtl is None:
                fixes.decision(f'`{namespace}:models/{obj_rel}` has no material library to be found')
                continue
            if (namespace, mtl) not in done_mtls:
                done_mtls.add((namespace, mtl))
                fix_mtl(content, namespace, models, mtl, fixes)
        # Libraries no OBJ names are put right too, so the content is all one way
        for library in sorted(models.rglob('*.mtl')):
            rel = library.relative_to(models).as_posix()
            if (namespace, rel) not in done_mtls:
                done_mtls.add((namespace, rel))
                fix_mtl(content, namespace, models, rel, fixes)


def fix_mtl(content, namespace, models, mtl, fixes):
    """The library's textures named by id (almura:block/x), as NeoForge reads them."""
    mtl_folder = mtl.rsplit('/', 1)[0] + '/' if '/' in mtl else ''
    mtl_lines, changed = [], []
    for line in read(models / mtl).splitlines():
        parts = line.strip().split()
        if len(parts) >= 2 and parts[0] in ('map_Kd', 'map_d', 'map_Ka', 'map_Ks'):
            texture = texture_of(content, namespace, mtl_folder, parts[-1])
            if texture is None:
                fixes.decision(f'`{namespace}:models/{mtl}`: texture `{parts[-1]}` isn\'t there')
            elif texture != parts[-1]:
                changed.append(f'{parts[-1]} → {texture}')
                parts[-1] = texture
                line = ' '.join(parts)
        mtl_lines.append(line)
    write(models / mtl, '\n'.join(mtl_lines) + '\n')
    if changed:
        fixes.add('OBJ material libraries', f'`{namespace}:models/{mtl}`: ' + ', '.join(sorted(set(changed))))


# 3. JSON block models -------------------------------------------------------------------------------------------------------------

def fix_models(content, fixes):
    for namespace in content.namespaces():
        models = content.ns(namespace) / 'models' / 'block'
        if not models.is_dir():
            continue
        for path in sorted(models.rglob('*.json')):
            try:
                model = lenient_json(read(path))
            except ValueError as error:
                fixes.decision(f'`{path.relative_to(content.assets).as_posix()}` isn\'t JSON: {error}')
                continue
            notes = []
            for element in model.get('elements', []):
                start, end = element.get('from'), element.get('to')
                if isinstance(start, list) and isinstance(end, list) and len(start) == 3 and len(end) == 3:
                    for axis in range(3):
                        if start[axis] > end[axis]:
                            start[axis], end[axis] = end[axis], start[axis]
                            notes.append('an element\'s corners put the right way round')
            textures = model.get('textures')
            if model.get('elements') and isinstance(textures, dict) and textures and 'particle' not in textures:
                first = next(iter(textures.values()))
                textures['particle'] = first
                notes.append(f'particle texture {first}')
            if notes:
                write(path, json.dumps(model, indent=4) + '\n')
                rel = path.relative_to(content.ns(namespace) / 'models').as_posix()
                fixes.add('Block models', f'`{namespace}:models/{rel}`: ' + '; '.join(sorted(set(notes))))


# 4. Drops -------------------------------------------------------------------------------------------------------------------------

def fix_drops(content, fixes):
    known = content.ids()
    for namespace in content.namespaces():
        for identifier, path in content.content_files(namespace, 'block'):
            text = read(path)
            try:
                block = lenient_json(text)
            except ValueError:
                continue
            missing = set()
            for state in (block.get('state') or {}).values():
                if not isinstance(state, dict):
                    continue
                for action in ((state.get('action') or {}).get('destroy') or []):
                    for drop in action.get('drop') or []:
                        item = drop.get('item') if isinstance(drop, dict) else None
                        if isinstance(item, str) and item.startswith(namespace + ':') and item not in known:
                            missing.add(item)
            for item in sorted(missing):
                own = identifier.rsplit('/', 1)[-1]
                theirs = item.rsplit('/', 1)[-1]
                if difflib.SequenceMatcher(None, own, theirs).ratio() < 0.6:
                    fixes.decision(f'`{identifier}` drops `{item}`, which doesn\'t exist (and isn\'t like the block\'s own name)')
                    continue
                text = text.replace(f'"{item}"', f'"{identifier}"')
                fixes.add('Drops of items that don\'t exist', f'`{identifier}`: `{item}` → itself')
            if missing:
                write(path, text)


# 5. Translations ------------------------------------------------------------------------------------------------------------------

MISSING_NAMES = {
    ('block', 'leaf'): {'kiwi_tree_fruit_leaf.name': 'Kiwi Tree Fruit Leaf'},
    ('block', 'slab'): {'stonebrick_cracked_slab.name': 'Cracked Stone Brick Slab', 'stonebrick_mossy_slab.name': 'Mossy Stone Brick Slab'},
}


def fix_translations(content, fixes):
    for namespace in content.namespaces():
        base = content.ns(namespace) / 'content'
        for path in sorted(base.rglob('_translations/*.json')):
            text = read(path)
            folder = path.parent.parent.relative_to(base).as_posix()
            changed = []

            def lower(match):
                key = match.group(1)
                if key == key.lower():
                    return match.group(0)
                changed.append(f'{key} → {key.lower()}')
                return f'"{key.lower()}"{match.group(2)}'

            text = re.sub(r'"([^"]+)"(\s*:)', lower, text)
            if path.name == 'en_us.json':
                kind_folder = tuple(folder.split('/', 1)) if '/' in folder else (folder, '')
                for key, name in MISSING_NAMES.get(kind_folder, {}).items():
                    if f'"{key}"' not in text:
                        text = re.sub(r'\{', '{\n  "' + key + '": ' + json.dumps(name) + ',', text, count=1)
                        changed.append(f'added {key}: {name}')
            if changed:
                write(path, text)
                fixes.add('Translations', f'`{namespace}/content/{folder}/_translations/{path.name}`: ' + ', '.join(changed))


# 6. Effect ids --------------------------------------------------------------------------------------------------------------------

def fix_effects(content, fixes):
    for namespace in content.namespaces():
        for identifier, path in content.content_files(namespace, 'item'):
            text = read(path)
            fixed = re.sub(r'"(minecraft:[A-Za-z_]*[A-Z][A-Za-z_]*)"', lambda m: f'"{m.group(1).lower()}"', text)
            if fixed != text:
                write(path, fixed)
                fixes.add('Effect ids', f'`{identifier}`: ' + ', '.join(sorted(set(re.findall(r'"(minecraft:[A-Za-z_]*[A-Z][A-Za-z_]*)"', text)))))


# 7. Wild crops --------------------------------------------------------------------------------------------------------------------

def ripe_ages(content):
    ages = {}
    for namespace in content.namespaces():
        for identifier, path in content.content_files(namespace, 'block'):
            if not identifier.split(':', 1)[1].startswith('crop/'):
                continue
            try:
                states = lenient_json(read(path)).get('state') or {}
            except ValueError:
                continue
            count = len([key for key in states if key.startswith('age=')])
            if count:
                ages[identifier] = count - 1
    return ages


def fix_wild_crops(content, fixes):
    ripe = ripe_ages(content)
    for namespace in content.namespaces():
        base = content.ns(namespace) / 'content' / 'grass'
        if not base.is_dir():
            continue
        for path in sorted(base.rglob('*.json')):
            text = read(path)
            changed = []

            def ripen(match):
                crop, age = match.group(2), int(match.group(4))
                want = ripe.get(crop)
                if want is None or want == age:
                    return match.group(0)
                changed.append(f'{crop} {age} → {want}')
                return f'{match.group(1)}{match.group(3)}{want}'

            text = re.sub(r'("id"\s*:\s*"((?:[a-z0-9_]+):crop/[a-z0-9_]+)"\s*,\s*)("state"\s*:\s*\{\s*"age"\s*:\s*)(\d+)', ripen, text)
            if changed:
                write(path, text)
                fixes.add('Wild crops made ripe', f'`{path.relative_to(content.assets).as_posix()}`: ' + ', '.join(changed))


# 8. Crop hydration ----------------------------------------------------------------------------------------------------------------

FARMLAND_LEVEL = re.compile(r'(\{\s*"id"\s*:\s*"minecraft:farmland")\s*,\s*"state"\s*:\s*\{\s*"level"\s*:\s*\{[^{}]*\}\s*\}(\s*\})')


def fix_hydration(content, fixes):
    count = 0
    for namespace in content.namespaces():
        for identifier, path in content.content_files(namespace, 'block'):
            if not identifier.split(':', 1)[1].startswith('crop/'):
                continue
            text = read(path)
            fixed, n = FARMLAND_LEVEL.subn(r'\1\2', text)
            if n:
                write(path, fixed)
                count += 1
                fixes.add('Crop hydration', f'`{identifier}`: farmland\'s "level" ({n}) removed')
    if count:
        fixes.decision(f'{count} crops asked farmland for "level", which it hasn\'t (its property is "moisture"); 1.12 ignored it, so '
                            'any farmland watered them, and that is kept. To make them need wet farmland as the content seems to have '
                            'meant, give them "moisture" with the range instead.')


# ----------------------------------------------------------------------------------------------------------------------------------

def known_decisions(content, fixes):
    """Mistakes the script can see but not fix without a choice."""
    for namespace in content.namespaces():
        for identifier, path in content.content_files(namespace, 'block'):
            if identifier.split(':', 1)[1].startswith('stair/prismarine'):
                if '"minecraft:stonebrick"' in read(path):
                    fixes.decision(f'`{identifier}` is made of `minecraft:stonebrick`: prismarine, prismarine bricks or dark prismarine?')
        for identifier, path in content.content_files(namespace, 'item'):
            if '/tool/' in identifier:
                model = content.ns(namespace) / 'models' / 'item' / (identifier.split(':', 1)[1] + '.json')
                if not model.is_file():
                    fixes.decision(f'`{identifier}` has no item model (`models/item/{identifier.split(":", 1)[1]}.json`)')


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument('--source', required=True, type=Path, help='the 3.0 content (the folder holding assets/); only read')
    parser.add_argument('--out', required=True, type=Path, help='where the 4.0 content goes')
    parser.add_argument('--replace', action='store_true', help='empty --out first')
    args = parser.parse_args()
    source, out = args.source.resolve(), args.out.resolve()
    if not (source / 'assets').is_dir():
        sys.exit(f'{source} has no assets folder')
    if out == source or source in out.parents or out in source.parents:
        sys.exit('--out must be apart from --source')
    if out.exists() and any(out.iterdir()):
        if not args.replace:
            sys.exit(f'{out} isn\'t empty (pass --replace to empty it)')
        shutil.rmtree(out)
    shutil.copytree(source / 'assets', out / 'assets', ignore=shutil.ignore_patterns('.git'))
    content, fixes = Content(out), Fixes()
    fix_file_names(content, fixes)
    fix_objs(content, fixes)
    fix_models(content, fixes)
    fix_drops(content, fixes)
    fix_translations(content, fixes)
    fix_effects(content, fixes)
    fix_wild_crops(content, fixes)
    fix_hydration(content, fixes)
    known_decisions(content, fixes)
    fixes.write(out / 'FIXES.md', source)
    for section, lines in fixes.done.items():
        print(f'{section}: {len(lines)}')
    print(f'Needs a decision: {len(fixes.decide)}')
    print(f'Written to {out}; see FIXES.md')


if __name__ == '__main__':
    main()
