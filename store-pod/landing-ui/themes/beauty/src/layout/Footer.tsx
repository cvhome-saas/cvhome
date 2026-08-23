import {getTranslations} from 'next-intl/server';
import {FacebookIcon, GithubIcon, InstagramIcon, LinkedinIcon, TwitterIcon, YoutubeIcon, type LucideIcon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';

const SOCIAL_ICONS: Record<string, LucideIcon> = {facebook: FacebookIcon, twitter: TwitterIcon, x: TwitterIcon, instagram: InstagramIcon, github: GithubIcon, youtube: YoutubeIcon, linkedin: LinkedinIcon};

/** The loading dock: a hazard band, four plates of quoted headings with mono rows, the store's own facts. */
export async function Footer({data}: { ctx: PageContext; data: LayoutData }) {
    const t = await getTranslations('COMPONENTS.FOOTER');
    const {store} = data;
    const shop = data.categories.filter(c => c.description).slice(0, 8);
    const info = data.pages.filter(p => p.description);
    // Footer-menu entries that are not pages, then the live legal policies — all resolved server-side.
    const more = [
        ...data.menus.footer.filter(n => n.kind !== 'PAGE' && n.href.startsWith('/')).map(n => ({key: n.href, href: n.href, label: n.label})),
        ...data.policies.map(pl => ({key: pl.href, href: pl.href, label: pl.title})),
    ];
    const socials = (store.socialLinks ?? []).filter(s => SOCIAL_ICONS[s.provider.toLowerCase()]);
    const col = 'flex flex-col border-foreground p-4 md:border-s first:md:border-s-0';
    const h = 'q mb-3 font-display text-sm font-semibold uppercase tracking-wide';
    const row = 'q py-1 font-mono text-xs uppercase tracking-wide hover:underline hover:underline-offset-4';
    return (
        <footer className="mt-section border-t border-foreground">
            <div className="hazard h-3 border-b border-foreground" aria-hidden/>
            <div className="mx-auto grid max-w-wide px-gutter md:grid-cols-4">
                <div className={col}>
                    <p className="q font-display text-2xl font-bold uppercase leading-none tracking-tight">{store.name}</p>
                    {(store.address?.address || store.address?.city) && (
                        <address dir="auto" className="mt-3 font-mono text-xs uppercase not-italic leading-relaxed tracking-wide text-muted-foreground">
                            {[store.address.address, store.address.city, store.address.postalCode, store.address.country].filter(Boolean).join(' · ')}
                        </address>
                    )}
                </div>
                {shop.length > 0 && (
                    <nav aria-label={t('SHOP')} className={col}>
                        <h2 className={h}>{t('SHOP')}</h2>
                        {shop.map(c => <Link key={c.code} prefetch={false} href={`/category/${c.description.friendlyUrl}`} className={row}>{c.description.name}</Link>)}
                    </nav>
                )}
                {(info.length > 0 || more.length > 0) && (
                    <nav aria-label={t('INFORMATION')} className={col}>
                        <h2 className={h}>{t('INFORMATION')}</h2>
                        {info.map(p => <Link key={p.code} prefetch={false} href={`/content/${p.description.friendlyUrl}`} className={row}>{p.description.name}</Link>)}
                        {more.map(l => <Link key={l.key} prefetch={false} href={l.href} className={row}>{l.label}</Link>)}
                    </nav>
                )}
                <div className={col}>
                    <h2 className={h}>{t('CONTACT')}</h2>
                    {store.email && <a href={`mailto:${store.email}`} className="py-1 font-mono text-xs tracking-wide hover:underline hover:underline-offset-4">{store.email}</a>}
                    {store.phone && <a href={`tel:${store.phone}`} className="py-1 font-mono text-xs tracking-wide hover:underline hover:underline-offset-4" dir="ltr">{store.phone}</a>}
                    {socials.length > 0 && (
                        <ul className="mt-3 flex" aria-label={t('FOLLOW_US')}>
                            {socials.map(s => {
                                const Icon = SOCIAL_ICONS[s.provider.toLowerCase()];
                                const url = s.url.startsWith('http') ? s.url : `https://${s.url}`;
                                return (
                                    <li key={s.provider} className="-ms-px first:ms-0">
                                        <a href={url} target="_blank" rel="noopener noreferrer" aria-label={s.provider} className="plate flex size-9 items-center justify-center hover:bg-foreground hover:text-background"><Icon className="size-4"/></a>
                                    </li>
                                );
                            })}
                        </ul>
                    )}
                </div>
            </div>
            <div className="border-t border-foreground">
                <p className="mx-auto max-w-wide px-gutter py-3 font-mono text-[0.7rem] uppercase tracking-wide text-muted-foreground">
                    © {new Date().getFullYear()} <span className="q">{store.name}</span> · {t('RIGHT_RESERVED')}
                </p>
            </div>
        </footer>
    );
}
