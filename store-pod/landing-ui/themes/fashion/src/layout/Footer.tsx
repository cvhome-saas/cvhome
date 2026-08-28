import {getTranslations} from 'next-intl/server';
import {FacebookIcon, GithubIcon, InstagramIcon, LinkedinIcon, TwitterIcon, YoutubeIcon, type LucideIcon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';

const SOCIAL_ICONS: Record<string, LucideIcon> = {
    facebook: FacebookIcon, twitter: TwitterIcon, x: TwitterIcon, instagram: InstagramIcon, github: GithubIcon,
    youtube: YoutubeIcon, linkedin: LinkedinIcon,
};

/** The foot of the wall: an ink baseboard. Store name in poster caps, then shop / information / contact columns, then the © line. */
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
    const heading = 'font-display text-sm uppercase tracking-wide text-background/60';
    const link = 'text-sm text-background/90 hover:text-background hover:underline hover:decoration-primary hover:decoration-2 hover:underline-offset-4';
    return (
        <footer className="mt-section bg-foreground text-background">
            <div className="mx-auto grid max-w-wide gap-10 px-gutter py-12 sm:grid-cols-2 lg:grid-cols-[2fr_1fr_1fr_1fr]">
                <div className="flex flex-col gap-3">
                    <p className="font-display text-3xl uppercase leading-[0.9] [overflow-wrap:anywhere] lg:text-4xl" dir="auto"><bdi>{store.name}</bdi></p>
                    {(store.address?.address || store.address?.city) && (
                        <address className="max-w-xs text-sm not-italic text-background/70">
                            {[store.address.address, store.address.city, store.address.postalCode, store.address.country].filter(Boolean).join(', ')}
                        </address>
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
                    {socials.length > 0 && (
                        <ul className="mt-2 flex gap-2" aria-label={t('FOLLOW_US')}>
                            {socials.map(s => {
                                const Icon = SOCIAL_ICONS[s.provider.toLowerCase()];
                                const url = s.url.startsWith('http') ? s.url : `https://${s.url}`;
                                return (
                                    <li key={s.provider}>
                                        <a href={url} target="_blank" rel="noopener noreferrer" aria-label={s.provider}
                                           className="flex size-9 items-center justify-center bg-background text-foreground hover:bg-primary hover:text-primary-foreground">
                                            <Icon className="size-4"/>
                                        </a>
                                    </li>
                                );
                            })}
                        </ul>
                    )}
                </div>
            </div>
            <div className="border-t border-background/15">
                <p className="mx-auto max-w-wide px-gutter py-4 text-xs text-background/60">
                    © {new Date().getFullYear()} {store.name}. {t('RIGHT_RESERVED')}
                </p>
            </div>
        </footer>
    );
}
