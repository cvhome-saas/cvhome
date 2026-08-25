import type {Metadata} from 'next';
import {getTheme} from '@/shell/theme/get-theme';
import {loadPolicy} from '@/shell/loaders/policy';
import {loadPageContext} from '@/shell/loaders/page-context';
import {pageMetadata} from '@/shell/seo/metadata';

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

export default async function PolicyPage({params, searchParams}: Props) {
    const [{type}, {v}] = await Promise.all([params, searchParams]);
    const [theme, ctx, data] = await Promise.all([getTheme(), loadPageContext(), loadPolicy(type, v ? Number(v) : undefined)]);
    return <theme.pages.Policy ctx={ctx} data={data}/>;
}
