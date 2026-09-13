import type {Metadata} from 'next';
import type {ThemeDefinition} from '@store-front/theme';
import {loadPolicy} from '@/shell/loaders/policy';
import {pageMetadata} from '@/shell/seo/metadata';
import {loadPageContext} from '@/shell/loaders/page-context';

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

export function policyPage(theme: ThemeDefinition) {
    return async function PolicyPage({params, searchParams}: Props) {
        const [{type}, {v}] = await Promise.all([params, searchParams]);
        const [ctx, data] = await Promise.all([loadPageContext(theme), loadPolicy(type, v ? Number(v) : undefined)]);
        return <theme.pages.Policy ctx={ctx} data={data}/>;
    };
}
