/**
 * The canonical section catalogue: every kind the builder can place, its shell-fallback variants, the
 * inspector's field schemas, and the presets the Add-section library offers. JSON-serializable on purpose —
 * `/api/theme-manifest` merges a theme's registry over this and serves it to the console, so the very data
 * the renderer resolves against is the data the builder's forms are generated from. Zero drift by construction.
 *
 * The content service keeps its own copy of just the kind ids (`LayoutKinds.java`) as a publish gate.
 */

import type {SectionKind} from '@store-front/types';

export type FieldType =
    'text' | 'textarea' | 'richtext' | 'select' | 'media' | 'toggle' | 'range' | 'color' | 'link'
    | 'ref:product-group' | 'ref:category' | 'ref:faq' | 'ref:post-category' | 'product-source';

export interface FieldLabel {
    en: string;
    ar: string;
}

export interface FieldSpec {
    /** Where the value lives: `props.<key>`, or `text.<key>` when `localized`. */
    key: string;
    type: FieldType;
    label: FieldLabel;
    /** Localized fields write into `text[key][locale]`; the rest into `props[key]`. */
    localized?: boolean;
    min?: number;
    max?: number;
    step?: number;
    options?: { value: string; label: FieldLabel }[];
    /** Show only while another prop equals a value (Shopify's visible_if, kept to the one shape we need). */
    visibleIf?: { key: string; equals: unknown };
}

export interface VariantSpec {
    id: string;
    label: FieldLabel;
}

export interface SectionPresetSpec {
    id: string;
    label: FieldLabel;
    kind: SectionKind;
    variant: string;
    props?: Record<string, unknown>;
    /** Item templates; the builder re-ids them on insert. */
    items?: { props?: Record<string, unknown>; text?: Record<string, Record<string, string>> }[];
    text?: Record<string, Record<string, string>>;
    style?: Record<string, unknown>;
}

export interface KindSpec {
    kind: SectionKind;
    label: FieldLabel;
    icon: string;
    /** The variants the SHELL fallback renders; themes may support a subset or add their own. */
    variants: VariantSpec[];
    fields: FieldSpec[];
    /** Present only for kinds with repeatable items. */
    itemFields?: FieldSpec[];
    itemLabel?: FieldLabel;
    maxItems?: number;
}

const l = (en: string, ar: string): FieldLabel => ({en, ar});
const v = (id: string, en: string, ar: string): VariantSpec => ({id, label: l(en, ar)});

const TITLE: FieldSpec = {key: 'title', type: 'text', label: l('Title', 'العنوان'), localized: true};

export const SECTION_CATALOG: readonly KindSpec[] = [
    {
        kind: 'hero', label: l('Hero', 'الواجهة'), icon: 'image',
        variants: [v('classic', 'Classic', 'كلاسيكي'), v('split', 'Split', 'مقسوم'),
            v('carousel', 'Carousel', 'شرائح'), v('minimal', 'Minimal', 'بسيط')],
        fields: [
            {key: 'heading', type: 'text', label: l('Heading', 'العنوان الرئيسي'), localized: true},
            {key: 'subheading', type: 'text', label: l('Subheading', 'العنوان الفرعي'), localized: true},
            {key: 'height', type: 'select', label: l('Height', 'الارتفاع'), options: [
                {value: 'sm', label: l('Short', 'قصير')}, {value: 'md', label: l('Medium', 'متوسط')},
                {value: 'lg', label: l('Tall', 'طويل')}]},
            {key: 'autoplay', type: 'toggle', label: l('Autoplay', 'تشغيل تلقائي')},
            {key: 'interval', type: 'range', label: l('Interval (s)', 'الفاصل (ث)'), min: 2, max: 12, step: 1,
                visibleIf: {key: 'autoplay', equals: true}},
        ],
        itemLabel: l('Slide', 'شريحة'), maxItems: 8,
        itemFields: [
            {key: 'mediaId', type: 'media', label: l('Image', 'الصورة')},
            {key: 'heading', type: 'text', label: l('Heading', 'العنوان'), localized: true},
            {key: 'subheading', type: 'text', label: l('Subheading', 'العنوان الفرعي'), localized: true},
            {key: 'cta', type: 'text', label: l('Button label', 'نص الزر'), localized: true},
            {key: 'link', type: 'link', label: l('Button links to', 'وجهة الزر')},
        ],
    },
    {
        kind: 'products', label: l('Products', 'المنتجات'), icon: 'shopping-bag',
        variants: [v('rail', 'Rail', 'شريط'), v('grid', 'Grid', 'شبكة')],
        fields: [
            TITLE,
            {key: 'subtitle', type: 'text', label: l('Subtitle', 'وصف قصير'), localized: true},
            {key: 'source', type: 'product-source', label: l('Products come from', 'مصدر المنتجات')},
            {key: 'limit', type: 'range', label: l('Products shown', 'عدد المنتجات'), min: 2, max: 16, step: 1},
            // A theme may spend this as a poster leading the grid (fashion's interleave); fallbacks ignore it.
            {key: 'mediaId', type: 'media', label: l('Lead image', 'صورة بارزة')},
        ],
    },
    {
        kind: 'categories', label: l('Categories', 'الفئات'), icon: 'layout-grid',
        variants: [v('grid', 'Grid', 'شبكة'), v('pills', 'Pills', 'أزرار')],
        fields: [
            TITLE,
            {key: 'limit', type: 'range', label: l('Categories shown', 'عدد الفئات'), min: 2, max: 12, step: 1},
        ],
    },
    {
        kind: 'promo', label: l('Promo strip', 'شريط ترويجي'), icon: 'tag',
        variants: [v('strip', 'Strip', 'شريط'), v('card', 'Card', 'بطاقة')],
        fields: [
            {key: 'message', type: 'text', label: l('Message', 'الرسالة'), localized: true},
            {key: 'cta', type: 'text', label: l('Link label', 'نص الرابط'), localized: true},
            {key: 'link', type: 'link', label: l('Links to', 'الوجهة')},
            {key: 'mediaId', type: 'media', label: l('Background image', 'صورة الخلفية')},
        ],
    },
    {
        kind: 'image', label: l('Image', 'صورة'), icon: 'image',
        variants: [v('full', 'Full width', 'عرض كامل'), v('contained', 'Contained', 'محدود')],
        fields: [
            {key: 'mediaId', type: 'media', label: l('Image', 'الصورة')},
            {key: 'alt', type: 'text', label: l('Alt text', 'النص البديل')},
            {key: 'caption', type: 'text', label: l('Caption', 'التسمية'), localized: true},
            {key: 'link', type: 'link', label: l('Links to', 'الوجهة')},
        ],
    },
    {
        kind: 'richtext', label: l('Rich text', 'نص منسق'), icon: 'align-left',
        variants: [v('default', 'Default', 'افتراضي'), v('centered', 'Centered', 'موسّط')],
        fields: [
            TITLE,
            {key: 'body', type: 'richtext', label: l('Body', 'المحتوى'), localized: true},
        ],
    },
    {
        kind: 'faq', label: l('FAQ', 'الأسئلة الشائعة'), icon: 'help-circle',
        variants: [v('accordion', 'Accordion', 'قائمة قابلة للطي')],
        fields: [
            TITLE,
            {key: 'group', type: 'ref:faq', label: l('FAQ group', 'مجموعة الأسئلة')},
            {key: 'limit', type: 'range', label: l('Questions shown', 'عدد الأسئلة'), min: 2, max: 12, step: 1},
        ],
    },
    {
        kind: 'posts', label: l('Blog posts', 'مقالات المدونة'), icon: 'newspaper',
        variants: [v('cards', 'Cards', 'بطاقات')],
        fields: [
            TITLE,
            {key: 'category', type: 'ref:post-category', label: l('Category', 'الفئة')},
            {key: 'limit', type: 'range', label: l('Posts shown', 'عدد المقالات'), min: 2, max: 6, step: 1},
        ],
    },
    {
        kind: 'testimonials', label: l('Testimonials', 'آراء العملاء'), icon: 'quote',
        variants: [v('cards', 'Cards', 'بطاقات'), v('quotes', 'Quotes', 'اقتباسات')],
        fields: [TITLE],
        itemLabel: l('Quote', 'اقتباس'), maxItems: 8,
        itemFields: [
            {key: 'quote', type: 'textarea', label: l('Quote', 'الاقتباس'), localized: true},
            {key: 'author', type: 'text', label: l('Author', 'الكاتب'), localized: true},
            {key: 'mediaId', type: 'media', label: l('Photo', 'الصورة')},
        ],
    },
    {
        kind: 'newsletter', label: l('Newsletter', 'النشرة البريدية'), icon: 'mail',
        variants: [v('inline', 'Inline', 'مضمّن'), v('boxed', 'Boxed', 'صندوق')],
        fields: [
            {key: 'heading', type: 'text', label: l('Heading', 'العنوان'), localized: true},
            {key: 'body', type: 'text', label: l('Supporting text', 'نص مساند'), localized: true},
            {key: 'cta', type: 'text', label: l('Button label', 'نص الزر'), localized: true},
        ],
    },
    {
        kind: 'usp', label: l('Trust badges', 'مزايا المتجر'), icon: 'shield',
        variants: [v('row', 'Row', 'صف')],
        fields: [TITLE],
        itemLabel: l('Badge', 'ميزة'), maxItems: 6,
        itemFields: [
            {key: 'icon', type: 'select', label: l('Icon', 'الأيقونة'), options: [
                {value: 'truck', label: l('Delivery', 'توصيل')}, {value: 'shield', label: l('Security', 'أمان')},
                {value: 'refresh', label: l('Returns', 'إرجاع')}, {value: 'star', label: l('Quality', 'جودة')},
                {value: 'headset', label: l('Support', 'دعم')}, {value: 'gift', label: l('Gifts', 'هدايا')}]},
            {key: 'title', type: 'text', label: l('Title', 'العنوان'), localized: true},
            {key: 'body', type: 'text', label: l('Text', 'النص'), localized: true},
        ],
    },
    {
        kind: 'video', label: l('Video', 'فيديو'), icon: 'play',
        variants: [v('embed', 'Embed', 'مضمّن')],
        fields: [
            TITLE,
            {key: 'url', type: 'text', label: l('Video URL (YouTube/Vimeo)', 'رابط الفيديو')},
        ],
    },
    {
        kind: 'brands', label: l('Brand logos', 'شعارات العلامات'), icon: 'award',
        variants: [v('row', 'Row', 'صف')],
        fields: [TITLE],
        itemLabel: l('Logo', 'شعار'), maxItems: 12,
        itemFields: [
            {key: 'mediaId', type: 'media', label: l('Logo', 'الشعار')},
            {key: 'link', type: 'link', label: l('Links to', 'الوجهة')},
        ],
    },
];

export const kindSpec = (kind: string): KindSpec | undefined => SECTION_CATALOG.find(k => k.kind === kind);

/** The kind's default variant — where an unknown or theme-unsupported variant lands. */
export const defaultVariant = (kind: string): string => kindSpec(kind)?.variants[0]?.id ?? 'default';

/** Finished-looking starting points for the Add-section library; the builder re-ids on insert. */
export const SECTION_PRESETS: readonly SectionPresetSpec[] = [
    {
        id: 'hero-text', label: l('Hero — text on color', 'واجهة — نص على لون'), kind: 'hero', variant: 'minimal',
        props: {height: 'md'},
        text: {heading: {en: 'Welcome to our store', ar: 'مرحبًا بكم في متجرنا'},
            subheading: {en: "Discover what's new", ar: 'اكتشف الجديد'}},
        style: {spacing: 'lg', width: 'full', tone: 'default'},
    },
    {
        id: 'hero-slides', label: l('Hero — image slides', 'واجهة — شرائح صور'), kind: 'hero', variant: 'carousel',
        props: {autoplay: true, interval: 5, height: 'lg'},
        items: [{text: {heading: {en: 'This season’s edit', ar: 'تشكيلة الموسم'},
            cta: {en: 'Shop now', ar: 'تسوق الآن'}}}],
        style: {spacing: 'none', width: 'full', tone: 'default'},
    },
    {
        id: 'products-featured', label: l('Featured products', 'منتجات مختارة'), kind: 'products', variant: 'grid',
        props: {source: {type: 'group', code: 'FEATURED_ITEMS'}, limit: 8},
        text: {title: {en: 'Featured', ar: 'مختاراتنا'}},
    },
    {
        id: 'products-newest', label: l('New arrivals', 'وصل حديثًا'), kind: 'products', variant: 'rail',
        props: {source: {type: 'newest'}, limit: 8},
        text: {title: {en: 'New arrivals', ar: 'وصل حديثًا'}},
    },
    {
        id: 'categories-grid', label: l('Category grid', 'شبكة الفئات'), kind: 'categories', variant: 'grid',
        props: {limit: 6}, text: {title: {en: 'Shop by category', ar: 'تسوق حسب الفئة'}},
    },
    {
        id: 'promo-strip', label: l('Promo strip', 'شريط ترويجي'), kind: 'promo', variant: 'strip',
        text: {message: {en: 'Free delivery on your first order', ar: 'توصيل مجاني لطلبك الأول'}},
        style: {spacing: 'sm', width: 'full', tone: 'inverse'},
    },
    {
        id: 'image-banner', label: l('Image banner', 'صورة عرض'), kind: 'image', variant: 'full', props: {}},
    {
        id: 'richtext-story', label: l('Story block', 'نص تعريفي'), kind: 'richtext', variant: 'centered',
        text: {body: {en: '<p>Tell your story here.</p>', ar: '<p>اكتب قصتك هنا.</p>'}},
    },
    {
        id: 'faq-top', label: l('FAQ', 'الأسئلة الشائعة'), kind: 'faq', variant: 'accordion', props: {limit: 5},
        text: {title: {en: 'Frequently asked questions', ar: 'الأسئلة الشائعة'}},
    },
    {
        id: 'posts-latest', label: l('Latest posts', 'أحدث المقالات'), kind: 'posts', variant: 'cards',
        props: {limit: 3}, text: {title: {en: 'From the journal', ar: 'من المدونة'}},
    },
    {
        id: 'testimonials-cards', label: l('Testimonials', 'آراء العملاء'), kind: 'testimonials', variant: 'cards',
        text: {title: {en: 'What customers say', ar: 'آراء عملائنا'}},
        items: [{text: {quote: {en: 'Wonderful service and fast delivery.', ar: 'خدمة رائعة وتوصيل سريع.'},
            author: {en: 'A happy customer', ar: 'عميل سعيد'}}}],
    },
    {
        id: 'newsletter-inline', label: l('Newsletter signup', 'الاشتراك في النشرة'), kind: 'newsletter',
        variant: 'inline', text: {heading: {en: 'Stay in the loop', ar: 'ابقَ على اطلاع'}},
    },
    {
        id: 'usp-row', label: l('Trust badges', 'مزايا المتجر'), kind: 'usp', variant: 'row',
        items: [
            {props: {icon: 'truck'}, text: {title: {en: 'Fast delivery', ar: 'توصيل سريع'}}},
            {props: {icon: 'shield'}, text: {title: {en: 'Secure payment', ar: 'دفع آمن'}}},
            {props: {icon: 'refresh'}, text: {title: {en: 'Easy returns', ar: 'إرجاع سهل'}}},
        ],
    },
    {
        id: 'video-embed', label: l('Video', 'فيديو'), kind: 'video', variant: 'embed', props: {}},
    {
        id: 'brands-row', label: l('Brand logos', 'شعارات العلامات'), kind: 'brands', variant: 'row', props: {}},
];
