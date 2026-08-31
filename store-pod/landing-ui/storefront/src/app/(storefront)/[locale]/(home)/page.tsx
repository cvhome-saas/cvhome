import {getTheme} from '@/shell/theme/get-theme';
import {loadHome} from '@/shell/loaders/home';
import {loadPageContext} from '@/shell/loaders/page-context';
import {SectionList} from '@/shell/sections/section-list';

/**
 * The home page is composed by the shell from the store's layout document — every store has one (the content
 * service materializes a starter default), so there is no theme-coded home any more. `?preview=<token>`
 * renders the draft for the builder's canvas.
 */
export default async function HomePage({searchParams}: {
    searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
    const {preview} = await searchParams;
    const token = typeof preview === 'string' && preview ? preview : undefined;
    const [theme, ctx, home] = await Promise.all([getTheme(), loadPageContext(), loadHome(token)]);
    return <SectionList theme={theme} ctx={ctx} home={home}/>;
}
