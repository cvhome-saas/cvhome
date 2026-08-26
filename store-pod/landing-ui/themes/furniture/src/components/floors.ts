import type {Category} from '@store-front/types';

/**
 * Floor numbering, in one place, so the number a shopper reads on the directory board is the number they
 * see again on the department plate and on a product's floor tag.
 *
 * The floor is the ROOT department's position in the merchant's own category order. A sub-category
 * (Women › Dresses) belongs to its root's floor, so the lookup walks the whole subtree rather than
 * matching the top level only.
 */
export interface Floor {
    /** Zero-padded floor number, e.g. "02". */
    number: string;
    category: Category;
}

const visible = (categories: Category[] | undefined) => (categories ?? []).filter(c => c.description && c.visible !== false);

function contains(category: Category, names: Set<string>, codes: Set<string>): boolean {
    if (names.has(category.description?.name) || codes.has(category.code)) return true;
    return (category.children ?? []).some(child => contains(child, names, codes));
}

/** The floor a category (or a product's breadcrumb trail) stands on; `undefined` when nothing matches. */
export function findFloor(tree: Category[] | undefined, names: Iterable<string>, code?: string): Floor | undefined {
    const roots = visible(tree);
    const nameSet = new Set(names);
    const codeSet = new Set(code ? [code] : []);
    const index = roots.findIndex(root => contains(root, nameSet, codeSet));
    return index >= 0 ? {number: String(index + 1).padStart(2, '0'), category: roots[index]} : undefined;
}

/** The roots, in board order, as the directory renders them. */
export function floors(tree: Category[] | undefined): Category[] {
    return visible(tree);
}
