import {strict as assert} from 'node:assert';
import {test} from 'node:test';
import type {LayoutSectionData} from '@store-front/types';
import {brandsModel, embedUrl, faqModel, heroModel, imageModel, promoModel, uspModel} from '../src/sections/models';

const section = (over: Partial<LayoutSectionData>): LayoutSectionData => ({
    id: 's1', kind: 'hero', variant: null, props: {}, items: null, text: {},
    style: null, hidden: false, devices: null, anchor: null, locked: null,
    ...over,
} as LayoutSectionData);

test('hero dedupes repeated CTA labels across the section and its slides', () => {
    const model = heroModel(section({
        text: {cta: 'Shop now'},
        props: {link: {type: 'category', value: 'men'}},
        items: [
            {id: 'a', props: {mediaUrl: '/a.jpg', link: {type: 'category', value: 'women'}}, text: {cta: 'shop NOW'}},
            {id: 'b', props: {mediaUrl: '/b.jpg', link: {type: 'category', value: 'kids'}}, text: {cta: 'Kids'}},
            {id: 'c', props: {link: {type: 'category', value: 'kids'}}, text: {cta: 'kids'}},
        ],
    }));
    assert.equal(model.cta?.href, '/category/men');
    assert.deepEqual(model.strips, [{label: 'Kids', href: '/category/kids'}]);
});

test('hero defaults: height md, autoplay on, interval 5', () => {
    const model = heroModel(section({}));
    assert.equal(model.height, 'md');
    assert.equal(model.autoplay, true);
    assert.equal(model.interval, 5);
});

test('a section CTA without a link target is not an action', () => {
    const model = heroModel(section({text: {cta: 'Shop now'}}));
    assert.equal(model.cta, undefined);
});

test('usp keeps only titled badges and passes the icon name through', () => {
    const badges = uspModel(section({
        items: [
            {id: 'a', props: {icon: 'truck'}, text: {title: 'Fast delivery', body: 'Under 48h'}},
            {id: 'b', props: {}, text: {title: '  '}},
        ],
    }));
    assert.equal(badges.length, 1);
    assert.deepEqual(badges[0], {id: 'a', icon: 'truck', title: 'Fast delivery', body: 'Under 48h'});
});

test('promo exposes the background artwork and drops a label-less link', () => {
    const model = promoModel(section({
        text: {message: 'Free delivery'},
        props: {mediaUrl: '/bg.jpg', link: {type: 'url', value: '/sale'}},
    }));
    assert.equal(model.backgroundSrc, '/bg.jpg');
    assert.equal(model.action, undefined);
});

test('image prefers the alt field, then the caption, and resolves its link', () => {
    const model = imageModel(section({
        variant: 'contained',
        props: {mediaUrl: '/x.jpg', alt: ' A look ', link: {type: 'product', value: 'p-1'}},
        text: {caption: 'The caption'},
    }));
    assert.equal(model.alt, 'A look');
    assert.equal(model.caption, 'The caption');
    assert.equal(model.href, '/product/p-1');
    assert.equal(model.contained, true);
});

test('faq flattens groups and honors the limit', () => {
    const entries = faqModel(section({props: {limit: 2}}), {
        faq: {groups: [
            {key: 'a', name: 'A', entries: [{question: 'q1', answer: 'a1'}, {question: 'q2', answer: 'a2'}]},
            {key: 'b', name: 'B', entries: [{question: 'q3', answer: 'a3'}]},
        ]},
    } as never);
    assert.deepEqual(entries.map(entry => entry.question), ['q1', 'q2']);
});

test('brands keeps a label with artwork or a name, never a blank one', () => {
    const brands = brandsModel(section({
        items: [
            {id: 'a', props: {mediaUrl: '/logo.png'}, text: {}},
            {id: 'b', props: {}, text: {name: 'Guerlain'}},
            {id: 'c', props: {}, text: {}},
        ],
    }));
    assert.deepEqual(brands.map(brand => brand.id), ['a', 'b']);
});

test('embedUrl maps only YouTube and Vimeo page urls', () => {
    assert.equal(embedUrl('https://www.youtube.com/watch?v=abc123'), 'https://www.youtube-nocookie.com/embed/abc123');
    assert.equal(embedUrl('https://youtu.be/abc123'), 'https://www.youtube-nocookie.com/embed/abc123');
    assert.equal(embedUrl('https://vimeo.com/12345'), 'https://player.vimeo.com/video/12345');
    assert.equal(embedUrl('https://example.com/watch?v=abc'), undefined);
    assert.equal(embedUrl('not a url'), undefined);
});
