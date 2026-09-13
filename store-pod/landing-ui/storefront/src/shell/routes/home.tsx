import {loadHome} from '@/shell/loaders/home';
import {SectionList} from '@/shell/sections/section-list';
import {themed, type ThemeSource} from './theme-source';

/**
 * The home page is composed by the shell from the store's layout document — every store has one (the content
 * service materializes a starter default), so there is no theme-coded home any more. `?preview=<token>`
 * renders the draft for the builder's canvas.
 */
export function homePage(theme: ThemeSource) {
    return async function HomePage({searchParams}: {
        searchParams: Promise<Record<string, string | string[] | undefined>>;
    }) {
        const {preview} = await searchParams;
        const token = typeof preview === 'string' && preview ? preview : undefined;
        const [{theme: resolved, ctx}, home] = await Promise.all([themed(theme), loadHome(token)]);
        return <SectionList theme={resolved} ctx={ctx} home={home}/>;
    };
}
