import {strict as assert} from 'node:assert';
import {test} from 'node:test';
import type {ComponentType} from 'react';
import {defineTheme} from '../src/define-theme';
import type {ThemeDefinition} from '../src/contract';

const Component = (() => null) as unknown as ComponentType<never>;

const skeletons = {home: Component, category: Component, product: Component, content: Component, checkout: Component, customer: Component, order: Component};

const pages = {
    Category: Component, Product: Component, Content: Component, BlogIndex: Component, BlogPost: Component, Faq: Component,
    Policy: Component, Checkout: Component, CheckoutResult: Component, Customer: Component, Order: Component,
};

const theme = (over: Partial<ThemeDefinition> = {}): ThemeDefinition => ({
    id: 'probe',
    name: 'Probe',
    version: '0.0.1',
    fonts: {variables: '', roles: {sans: '--font-sans'}},
    tokens: {defaultColors: {background: '#ffffff', primary: '#000000'} as ThemeDefinition['tokens']['defaultColors']},
    layout: {config: {} as ThemeDefinition['layout']['config'], Root: Component},
    pages,
    states: {PageSkeleton: skeletons, ErrorState: Component, NotFound: Component, EmptyState: Component, Redirecting: Component},
    ...over,
} as ThemeDefinition);

test('a theme without Login, Register or Search still satisfies the contract', () => {
    assert.equal(defineTheme(theme()).id, 'probe');
});

test('a theme that brings Login and Register keeps them', () => {
    const def = defineTheme(theme({pages: {...pages, Login: Component, Register: Component}}));
    assert.equal(typeof def.pages.Login, 'function');
    assert.equal(typeof def.pages.Register, 'function');
});

test('a required page is still required', () => {
    const rest = Object.fromEntries(Object.entries(pages).filter(([name]) => name !== 'Customer'));
    assert.throws(() => defineTheme(theme({pages: rest as unknown as ThemeDefinition['pages']})), /pages\.Customer/);
});
