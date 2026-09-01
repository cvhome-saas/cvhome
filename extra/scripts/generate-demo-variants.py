#!/usr/bin/env python3
"""Expand the demo stores so >=75% of every store's products sell by variants.

Appends a generated block to each store's catalog seed 18 and inventory seed 18. The hand-written
"curated" products at the top of each catalog file are left exactly as they are: QA and the
integration tests pin their skus and their deliberately-missing combinations.
"""
import re
from decimal import Decimal, ROUND_HALF_UP
from pathlib import Path

# repo root, from extra/scripts/
ROOT = Path(__file__).resolve().parents[2]
CAT = ROOT / "store-pod/catalog/catalog-service/src/main/resources/init-sql/stores"
INV = ROOT / "store-pod/inventory/inventory-service/src/main/resources/init-sql/stores"

FASHION = "65f023632bc46470c104b76f"
BEAUTY = "65f020632bc46470c104b76f"
CARS = "65f023632bc26470c104b75f"
ELEC = "65f023632bc46470c104b75f"

# ---------------------------------------------------------------------------- new option values
# (value_id, option_id, code, sort_order, name_lang1, name_lang2)
NEW_VALUES = {
    FASHION: [
        (20, 1, "black", 2, "Black", "أسود"),
        (21, 1, "white", 3, "White", "أبيض"),
        (22, 1, "green", 4, "Green", "أخضر"),
        (23, 2, "s", 0, "S", "صغير"),
        (24, 2, "xl", 3, "XL", "كبير جداً"),
    ],
    BEAUTY: [
        (25, 3, "rose", 3, "Rose", "Rosé"),
        (26, 3, "sand", 4, "Sand", "Sable"),
        (27, 4, "100ml", 2, "100 ml", "100 ml"),
        (28, 4, "200ml", 3, "200 ml", "200 ml"),
    ],
    CARS: [
        (29, 5, "blue", 3, "أزرق", "Bleu"),
        (30, 5, "red", 4, "أحمر", "Rouge"),
        (31, 6, "sport", 2, "رياضي", "Sport"),
    ],
    ELEC: [
        (32, 7, "1tb", 3, "1 TB", "1 To"),
        (33, 8, "midnight", 2, "Midnight", "Minuit"),
        (34, 8, "gold", 3, "Gold", "Or"),
    ],
}

# the existing m/l pair was seeded before S and XL existed; re-sort so a size picker reads S M L XL
SORT_FIXES = {FASHION: [(3, 1), (4, 2)]}

LANGS = {FASHION: ("en", "ar"), BEAUTY: ("en", "fr"), CARS: ("ar", "fr"), ELEC: ("en", "fr")}

# option_id -> ordered value ids, per store
AXES = {
    FASHION: {"color": (1, [1, 2, 20, 21, 22]), "size": (2, [23, 3, 4, 24])},
    BEAUTY: {"shade": (3, [5, 6, 7, 25, 26]), "volume": (4, [8, 9, 27, 28])},
    CARS: {"paint": (5, [10, 11, 12, 29, 30]), "trim": (6, [13, 14, 31])},
    ELEC: {"storage": (7, [15, 16, 17, 32]), "finish": (8, [18, 19, 33, 34])},
}

VALUE_CODE = {}
for store, values in NEW_VALUES.items():
    for vid, _oid, code, *_rest in values:
        VALUE_CODE[vid] = code
VALUE_CODE.update({
    1: "red", 2: "blue", 3: "m", 4: "l",
    5: "fair", 6: "medium", 7: "deep", 8: "30ml", 9: "50ml",
    10: "white", 11: "black", 12: "silver", 13: "standard", 14: "premium",
    15: "128gb", 16: "256gb", 17: "512gb", 18: "graphite", 19: "silver",
})

# products the curated head of each file already turns into multi-variant products
CURATED = {FASHION: {1, 2}, BEAUTY: {46, 47, 48}, CARS: {91, 92, 93}, ELEC: {136, 137, 138}}
# products that stay single-variant: the control case the storefront and the tests need. 3 and 4 are
# pinned by ProductVariantApiIntegrationTest and ProductApiIntegrationTest respectively.
SIMPLE = {
    FASHION: {3, 4, 11, 16, 21, 26, 31, 36, 41},
    BEAUTY: {51, 56, 61, 66, 71, 76, 81, 86, 90},
    CARS: {96, 101, 106, 111, 116, 121, 126, 131, 135},
    ELEC: {141, 146, 151, 156, 161, 166, 171, 176, 180},
}
FIRST_VARIANT_ID = {FASHION: 1001, BEAUTY: 2001, CARS: 3001, ELEC: 4001}

# shape cycles: each entry is a list of (axis name, how many of its values to use)
SHAPES = {
    FASHION: {
        "sized": [[("size", 3)], [("color", 2), ("size", 2)], [("size", 4)],
                  [("color", 3), ("size", 2)], [("color", 2), ("size", 3)], [("size", 2)]],
        "colored": [[("color", 2)], [("color", 3)], [("color", 4)], [("color", 3)]],
    },
    BEAUTY: {
        "shaded": [[("shade", 3)], [("shade", 2), ("volume", 2)], [("shade", 4)],
                   [("shade", 3), ("volume", 2)], [("shade", 2)], [("shade", 2), ("volume", 3)]],
        "sized": [[("volume", 2)], [("volume", 3)], [("volume", 4)], [("volume", 3)]],
    },
    CARS: {
        "all": [[("paint", 3)], [("paint", 2), ("trim", 2)], [("trim", 3)],
                [("paint", 3), ("trim", 2)], [("paint", 4)], [("paint", 2), ("trim", 3)]],
    },
    ELEC: {
        "all": [[("storage", 3)], [("storage", 2), ("finish", 2)], [("finish", 3)],
                [("storage", 3), ("finish", 2)], [("storage", 4)], [("storage", 2), ("finish", 3)]],
    },
}


def family(store, sku):
    if store == FASHION:
        return "colored" if ("-BG-" in sku or "-AC-" in sku) else "sized"
    if store == BEAUTY:
        return "shaded" if "-MAKE-" in sku else "sized"
    return "all"


def read_defaults(store):
    """product_id -> original sku, from the default-variant seed."""
    text = (CAT / store / "17-catalog-default-variants.sql").read_text()
    pattern = re.compile(r"VALUES \((\d+), NOW\(\), NOW\(\), '[^']+', \d+, '([^']+)'")
    return {int(pid): sku for pid, sku in pattern.findall(text)}


def read_prices(store):
    """product_id -> base price, walking the availability/price seed a statement at a time."""
    text = (INV / store / "17-inventory-availability-price.sql").read_text()
    avail, prices, table = {}, {}, None
    for line in text.splitlines():
        if "INSERT INTO inventory.product_availability" in line:
            table = "avail"
        elif "INSERT INTO inventory.product_price" in line:
            table = "price"
        elif line.startswith("VALUES ("):
            fields = [f.strip().strip("'") for f in line[len("VALUES ("):line.rindex(")")].split(",")]
            if table == "avail":
                avail[fields[0]] = int(fields[2])
            elif table == "price":
                prices[avail[fields[2]]] = Decimal(fields[5])
            table = None
    return prices


QUANTITIES = [14, 9, 6, 0, 11, 7, 3, 22, 5, 18]


def cartesian(axes):
    rows = [[]]
    for _name, ids in axes:
        rows = [row + [vid] for row in rows for vid in ids]
    return rows


def generate(store):
    defaults = read_defaults(store)
    prices = read_prices(store)
    axes_of = AXES[store]
    shapes = SHAPES[store]
    cat, inv = [], []
    variant_id = FIRST_VARIANT_ID[store]
    counters = {key: 0 for key in shapes}
    varianted = len(CURATED[store])

    for product_id in sorted(defaults):
        if product_id in CURATED[store] or product_id in SIMPLE[store]:
            continue
        base_sku = defaults[product_id]
        key = family(store, base_sku)
        shape = shapes[key][counters[key] % len(shapes[key])]
        counters[key] += 1
        varianted += 1

        axes = [(name, axes_of[name][1][:count]) for name, count in shape]
        combos = cartesian(axes)
        option_ids = [axes_of[name][0] for name, _ in axes]

        cat.append("-- product %d (%s): %s" % (
            product_id, base_sku,
            " x ".join("%s(%d)" % (name, len(ids)) for name, ids in axes)))
        for sort_order, (name, _ids) in enumerate(axes):
            cat.append("INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)")
            cat.append("VALUES (%d, %d, %d) on conflict (product_id, product_option_id) do nothing;"
                       % (product_id, axes_of[name][0], sort_order))

        for index, combo in enumerate(combos):
            signature = "-".join(str(v) for v in sorted(combo))
            if index == 0:
                cat.append("UPDATE catalog.product_variant SET option_signature = '%s' WHERE product_variant_id = %d;"
                           % (signature, product_id))
                target = product_id
            else:
                sku = base_sku + "".join("-" + VALUE_CODE[v].upper() for v in combo)
                target = variant_id
                variant_id += 1
                cat.append("INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified,"
                           " store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)")
                cat.append("VALUES (%d, NOW(), NOW(), '%s', %d, '%s', %d, false, '%s')"
                           % (target, store, product_id, sku, index, signature))
                cat.append("on conflict (product_variant_id) do nothing;")
                base = prices.get(product_id, Decimal("100.00"))
                amount = (base * (1 + Decimal("0.05") * index)).quantize(Decimal("0.01"), ROUND_HALF_UP)
                quantity = QUANTITIES[(product_id + index) % len(QUANTITIES)]
                inv.append("INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id,"
                           " product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)")
                inv.append("VALUES (%d, '%s', %d, '%s', %d, true, 1, 0)"
                           % (target, store, product_id, sku, quantity))
                inv.append("on conflict (product_avail_id) do nothing;")
                inv.append("INSERT INTO inventory.product_price (product_price_id, store_merchant_id,"
                           " product_avail_id, product_price_code, default_price, product_price_amount)")
                inv.append("VALUES (%d, '%s', %d, 'base', true, %s)" % (target, store, target, amount))
                inv.append("on conflict (product_price_id) do nothing;")
                inv.append("")
            for option_id, value_id in zip(option_ids, combo):
                cat.append("INSERT INTO catalog.product_variant_option_value (product_variant_id,"
                           " product_option_id, product_option_value_id)")
                cat.append("VALUES (%d, %d, %d) on conflict (product_variant_id, product_option_id) do nothing;"
                           % (target, option_id, value_id))
        cat.append("")
    return cat, inv, varianted, len(defaults), variant_id


def vocabulary(store):
    lang1, lang2 = LANGS[store]
    lines = []
    for value_id, option_id, code, sort_order, name1, name2 in NEW_VALUES[store]:
        lines.append("INSERT INTO catalog.product_option_value (product_option_value_id, date_created,"
                     " date_modified, code, sort_order, product_option_id)")
        lines.append("VALUES (%d, NOW(), NOW(), '%s', %d, %d) on conflict (product_option_value_id) do nothing;"
                     % (value_id, code, sort_order, option_id))
        for lang, name in ((lang1, name1), (lang2, name2)):
            description_id = next(DESCRIPTION_IDS)
            lines.append("INSERT INTO catalog.product_option_value_description (description_id, date_created,"
                         " date_modified, name, language_code, product_option_value_id)")
            lines.append("VALUES (%d, NOW(), NOW(), '%s', '%s', %d) on conflict (description_id) do nothing;"
                         % (description_id, name.replace("'", "''"), lang, value_id))
        lines.append("")
    for value_id, sort_order in SORT_FIXES.get(store, []):
        lines.append("UPDATE catalog.product_option_value SET sort_order = %d WHERE product_option_value_id = %d;"
                     % (sort_order, value_id))
    if SORT_FIXES.get(store):
        lines.append("")
    return lines


def counter(start):
    while True:
        yield start
        start += 1


DESCRIPTION_IDS = counter(39)

HEADER = """
-- ---------------------------------------------------------------------------------------------------------
-- Generated bulk: the rest of this store's catalogue sells by variants too.
--
-- The curated products above stay exactly as they are (QA and the integration tests pin their skus and
-- their deliberately-missing combinations). Everything below gives {varianted} of the store's {total} products
-- ({percent}%) at least two variants, leaving {simple} deliberately optionless as the control case. Matrix shapes
-- rotate — one axis, two axes, two to six combinations — so listings, facets and the console matrix all
-- meet realistic shapes, and the stores carry enough rows to be worth measuring.
--
-- Regenerated by extra/scripts/generate-demo-variants.py; edit that rather than these lines.
-- ---------------------------------------------------------------------------------------------------------
"""

INV_HEADER = """
-- ---------------------------------------------------------------------------------------------------------
-- Generated bulk: stock and price for the combination skus of 18-catalog-options-variants.sql.
--
-- One row per combination beyond the default variant, whose row the base seed already carries. Prices step
-- up with the combination so a matrix shows real per-variant pricing, and the quantity cycle leaves a few
-- combinations at zero — the greyed-chip case on the PDP.
--
-- Regenerated by extra/scripts/generate-demo-variants.py; edit that rather than these lines.
-- ---------------------------------------------------------------------------------------------------------
"""

MARKER = "-- Generated bulk:"


def strip_generated(text):
    index = text.find(MARKER)
    if index == -1:
        return text.rstrip() + "\n"
    return text[:text.rindex("-- ---", 0, index)].rstrip() + "\n"


summary = []
for store in (FASHION, BEAUTY, CARS, ELEC):
    cat_lines, inv_lines, varianted, total, next_variant = generate(store)
    percent = round(varianted * 100 / total)
    header = HEADER.format(varianted=varianted, total=total, percent=percent,
                           simple=total - varianted)

    cat_path = CAT / store / "18-catalog-options-variants.sql"
    body = strip_generated(cat_path.read_text())
    cat_path.write_text(body + header + "\n" + "\n".join(vocabulary(store) + cat_lines).rstrip() + "\n")

    inv_path = INV / store / "18-inventory-variant-price.sql"
    body = strip_generated(inv_path.read_text()) if inv_path.exists() else ""
    inv_path.write_text(body + INV_HEADER + "\n" + "\n".join(inv_lines).rstrip() + "\n")

    summary.append((store, varianted, total, percent, next_variant - FIRST_VARIANT_ID[store]))

for store, varianted, total, percent, extras in summary:
    print("%s  %2d/%d varianted (%d%%)  %3d extra variant rows" % (store, varianted, total, percent, extras))
print("last option value description id:", next(DESCRIPTION_IDS) - 1)
