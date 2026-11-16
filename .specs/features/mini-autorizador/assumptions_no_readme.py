"""Prova do C26: cada linha de `## Assumptions` do plano aparece no README pelo nome."""
import re
import sys

plano, readme = (open(p, encoding="utf-8").read() for p in sys.argv[1:3])
secao = re.split(r"^## Assumptions$", plano, maxsplit=1, flags=re.M)[1].split("\n## ", 1)[0]
nomes = [
    linha.split("|")[1].strip()
    for linha in secao.splitlines()
    if linha.startswith("| ") and not linha.startswith("| Assumption") and not linha.startswith("| ---")
]
faltando = [n for n in nomes if re.sub(r"`", "", n).lower() not in re.sub(r"`", "", readme).lower()]
for n in faltando:
    print(f"falta no README: {n}")
sys.exit(1 if faltando or not nomes else 0)
