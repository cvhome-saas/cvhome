import {getTranslations} from 'next-intl/server';
import {FacebookIcon, GithubIcon, InstagramIcon, LinkedinIcon, TwitterIcon, YoutubeIcon, type LucideIcon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';

const SOCIAL_ICONS: Record<string, LucideIcon> = {
    facebook: FacebookIcon, twitter: TwitterIcon, x: TwitterIcon, instagram: InstagramIcon, github: GithubIcon,
    youtube: YoutubeIcon, linkedin: LinkedinIcon,
};

/** The loading dock: an ink field closing the floor — the store name at signage scale, then the manifest columns. */
export async function Footer({data}: { ctx: PageContext; data: LayoutData }) {
    const t = await getTranslations('COMPONENTS.FOOTER');
    const {store} = data;
    const shop = data.categories.filter(c => c.description).slice(0, 8);
    const info = data.pages;
    // Footer-menu entries that are not pages, then the live legal policies — all resolved server-side.
    const more = [
        ...data.menus.footer.filter(n => n.kind !== 'PAGE' && n.href.startsWith('/')).map(n => ({key: n.href, href: n.href, label: n.label})),
        ...data.policies.map(pl => ({key: pl.href, href: pl.href, label: pl.title})),
    ];
    const socials = data.socialLinks.filter(s => SOCIAL_ICONS[s.provider.toLowerCase()]);
    const heading = 'signage text-base text-background/70';
    const link = 'text-sm text-background/85 hover:text-background hover:underline';
    return (
        <footer className="mt-section bg-foreground text-background">
            <div className="mx-auto max-w-content px-gutter pb-12 pt-10">
                <p className="signage text-4xl leading-none sm:text-5xl"><bdi dir="auto">{store.name}</bdi></p>
                <div className="mt-8 grid gap-10 border-t-2 border-background/20 pt-8 sm:grid-cols-2 lg:grid-cols-4">
                    <div className="flex flex-col gap-3">
                        {(store.address?.address || store.address?.city) && (
                            <address className="text-sm not-italic text-background/85">
                                {[store.address.address, store.address.city, store.address.postalCode, store.address.country].filter(Boolean).join(', ')}
                            </address>
                        )}
                        {socials.length > 0 && (
                            <ul className="mt-1 flex gap-2" aria-label={t('FOLLOW_US')}>
                                {socials.map(s => {
                                    const Icon = SOCIAL_ICONS[s.provider.toLowerCase()];
                                    const url = s.url.startsWith('http') ? s.url : `https://${s.url}`;
                                    return (
                                        <li key={s.provider}>
                                            <a href={url} target="_blank" rel="noopener noreferrer" aria-label={s.provider}
                                               className="flex size-10 items-center justify-center rounded-control border-2 border-background/25 transition-colors duration-(--motion-fast) hover:border-background/60">
                                                <Icon className="size-4"/>
                                            </a>
                                        </li>
                                    );
                                })}
                            </ul>
                        )}
                    </div>
                    {shop.length > 0 && (
                        <nav aria-label={t('SHOP')} className="flex flex-col gap-2">
                            <h2 className={heading}>{t('SHOP')}</h2>
                            {shop.map(c => <Link key={c.code} prefetch={false} href={`/category/${c.description.friendlyUrl}`} className={link}>{c.description.name}</Link>)}
                        </nav>
                    )}
                    {(info.length > 0 || more.length > 0) && (
                        <nav aria-label={t('INFORMATION')} className="flex flex-col gap-2">
                            <h2 className={heading}>{t('INFORMATION')}</h2>
                            {info.map(p => <Link key={p.code} prefetch={false} href={p.href} className={link}>{p.name}</Link>)}
                            {more.map(l => <Link key={l.key} prefetch={false} href={l.href} className={link}>{l.label}</Link>)}
                        </nav>
                    )}
                    <div className="flex flex-col gap-2">
                        <h2 className={heading}>{t('CONTACT')}</h2>
                        {store.email && <a href={`mailto:${store.email}`} className={link}>{store.email}</a>}
                        {store.phone && <a href={`tel:${store.phone}`} className={link} dir="ltr">{store.phone}</a>}
                    </div>
                </div>
            </div>
            <div className="border-t-2 border-background/20">
                <p className="mx-auto max-w-content px-gutter py-4 text-xs text-background/70">
                    © {new Date().getFullYear()} {store.name}. {t('RIGHT_RESERVED')}
                </p>
            </div>
        </footer>
    );
}
