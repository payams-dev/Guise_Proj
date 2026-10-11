"""Offline validation fallback; normal release build remains Gradle lwjgl3:jar.
Usage: python tools/package-local.py /path/to/temporary/dependency-directory
The directory contains guise-gdx.jar, guise-gson.jar, and guise-libs/*.jar.
"""
from pathlib import Path
import hashlib, json, subprocess, sys, zipfile
root = Path(__file__).resolve().parents[1]
deps = Path(sys.argv[1] if len(sys.argv) > 1 else '/tmp')
jars = [deps/'guise-gdx.jar', deps/'guise-gson.jar'] + sorted((deps/'guise-libs').glob('*.jar'))
if any(not p.is_file() for p in jars) or len(jars) < 10:
    raise SystemExit('Missing pinned runtime jars; see VALIDATION.md')
classes = root/'build/portable-classes'
classes.mkdir(parents=True, exist_ok=True)
sources = sorted((root/'core/src/main/java').rglob('*.java')) + sorted((root/'lwjgl3/src/main/java').rglob('*.java'))
import os
subprocess.run(['java', 'com.sun.tools.javac.Main', '--release', '11', '-encoding', 'UTF-8', '-cp', os.pathsep.join(map(str,jars)), '-d', str(classes)]+list(map(str,sources)), check=True)
out = root/'dist/GUISE-Band-A.jar'
out.parent.mkdir(exist_ok=True)
written=set()
temp = out.with_name('.GUISE-Band-A-complete.tmp')
with zipfile.ZipFile(temp,'w',zipfile.ZIP_DEFLATED) as archive:
    archive.writestr('META-INF/MANIFEST.MF','Manifest-Version: 1.0\r\nMain-Class: com.payamsdev.oldsona.lwjgl3.Lwjgl3Launcher\r\nMulti-Release: true\r\nEnable-Native-Access: ALL-UNNAMED\r\n\r\n')
    written.add('META-INF/MANIFEST.MF')
    for directory in [classes,root/'assets',root/'lwjgl3/src/main/resources']:
        for p in sorted(directory.rglob('*')):
            if p.is_file():
                name=p.relative_to(directory).as_posix()
                if name not in written:archive.write(p,name);written.add(name)
    for jar in jars:
        with zipfile.ZipFile(jar) as dependency:
            for info in dependency.infolist():
                name=info.filename
                if info.is_dir() or name.endswith('module-info.class') or name.upper().endswith(('.SF','.DSA','.RSA')) or name=='META-INF/INDEX.LIST':continue
                if 'LICENSE' in name.upper() or 'NOTICE' in name.upper():
                    archive.writestr('META-INF/third-party/'+jar.name+'/'+name,dependency.read(info))
                if name not in written:archive.writestr(name,dependency.read(info));written.add(name)
    archive.writestr('META-INF/GUISE-runtime-inputs.json',json.dumps({p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in jars},indent=2))
with zipfile.ZipFile(temp) as verify:
    if verify.testzip() is not None: raise SystemExit('Archive verification failed')
os.replace(temp, out)
print(str(out),out.stat().st_size,'bytes')
