import {getTranslations} from 'next-intl/server';
import type {RootLayoutProps} from '@store-front/theme';
import {Announcement} from '../client';
import {Header} from './Header';
import {Footer} from './Footer';

/** Page frame: skip link → announcement → header → main → footer. Server component. */
export async function Root({ctx, data, children}: RootLayoutProps) {
    const t = await getTranslations('COMMON');
    return (
        <>
            <a href="#main"
               className="sr-only focus:not-sr-only focus:fixed focus:start-2 focus:top-2 focus:z-50 focus:rounded-control focus:bg-primary focus:px-3 focus:py-2 focus:text-primary-foreground">
                {t('SKIP_TO_CONTENT')}
            </a>
            {data.announcement && <Announcement announcement={data.announcement}/>}
            <Header ctx={ctx} data={data}/>
            <main id="main" className="flex-1">{children}</main>
            <Footer ctx={ctx} data={data}/>
        </>
    );
}
