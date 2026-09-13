import type {Metadata} from 'next';
import {loadPolicy} from '@/shell/loaders/policy';
import {pageMetadata} from '@/shell/seo/metadata';
import {themed, type ThemeSource} from './theme-source';

type Props = { params: Promise<{ type: string }>; searchParams: Promise<{ v?: string }> };

export async function generateMetadata({params, searchParams}: Props): Promise<Metadata> {
    const [{type}, {v}] = await Promise.all([params, searchParams]);
    try {
        const {policy} = await loadPolicy(type, v ? Number(v) : undefined);
        return pageMetadata(policy.heading);
    } catch {
        return {};
    }
}

export function policyPage(theme: ThemeSource) {
    return async function PolicyPage({params, searchParams}: Props) {
        const [{type}, {v}] = await Promise.all([params, searchParams]);
        const [{theme: resolved, ctx}, data] = await Promise.all([themed(theme), loadPolicy(type, v ? Number(v) : undefined)]);
        return <resolved.pages.Policy ctx={ctx} data={data}/>;
    };
}
