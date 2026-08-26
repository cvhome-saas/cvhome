'use client'
import {useState} from 'react';
import {useTranslations} from 'next-intl';
import {GlobeIcon, ShoppingBagIcon, UserIcon} from 'lucide-react';
import {Link, usePathname, useRouter} from '@store-front/i18n/navigation';
import type {PageContext} from '@store-front/theme';
import {useCart} from '@store-front/hooks/use-cart';
import {useUser} from '@store-front/hooks/use-user';
import {Button} from '@store-front/ui/button';
import {DropdownMenu, DropdownMenuContent, DropdownMenuGroup, DropdownMenuItem, DropdownMenuTrigger} from '@store-front/ui/dropdown-menu';
import {cn} from '@store-front/ui/lib/utils';
import {Figure} from '../components/Figure';
import {CartDrawer} from './CartDrawer';

/**
 * The utility rail: language, account, and the basket as a cloakroom ticket — an outlined plate carrying
 * the tally in the same tabular slot every other figure on the site uses, rolling when it changes.
 * Everything inherits `currentColor` so the same three controls work on the ink rail and on the masthead.
 */
const CONTROL = 'text-current hover:bg-current/15 hover:text-current focus-visible:bg-current/15';

export function HeaderActions({ctx, className}: { ctx: PageContext; className?: string }) {
    return (
        <div className={cn('flex items-center gap-1', className)}>
            <LocaleSwitcher ctx={ctx}/>
            <AccountButton ctx={ctx}/>
            <CartTicket ctx={ctx}/>
        </div>
    );
}

function LocaleSwitcher({ctx}: { ctx: PageContext }) {
    const t = useTranslations('COMPONENTS.HEADER.LANGUAGE');
    const router = useRouter();
    const pathname = usePathname();
    const languages = ctx.store.supportedLanguages ?? [];
    if (languages.length <= 1) return null;
    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon-sm" className={CONTROL} aria-label={t('CHANGE_LANGUAGE')}><GlobeIcon/></Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
                <DropdownMenuGroup>
                    {languages.map(lang => (
                        <DropdownMenuItem key={lang} disabled={lang === ctx.locale} onClick={() => router.replace(pathname, {locale: lang})}>
                            {t(lang.toUpperCase())}
                        </DropdownMenuItem>
                    ))}
                </DropdownMenuGroup>
            </DropdownMenuContent>
        </DropdownMenu>
    );
}

function AccountButton({ctx}: { ctx: PageContext }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const {user, login, logout} = useUser(ctx.storeContext);
    if (!user) {
        return <Button variant="ghost" size="icon-sm" className={CONTROL} aria-label={t('LOGIN')} onClick={login}><UserIcon/></Button>;
    }
    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon-sm" className={CONTROL} aria-label={t('ACCOUNT')}><UserIcon/></Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
                <DropdownMenuGroup>
                    <DropdownMenuItem asChild><Link prefetch={false} href="/customer">{t('PROFILE')}</Link></DropdownMenuItem>
                    <DropdownMenuItem onClick={logout} variant="destructive">{t('LOGOUT')}</DropdownMenuItem>
                </DropdownMenuGroup>
            </DropdownMenuContent>
        </DropdownMenu>
    );
}

function CartTicket({ctx}: { ctx: PageContext }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const pathname = usePathname();
    const {count} = useCart(ctx.storeContext);
    // Open state is keyed to the path it was opened on, so a navigation closes it without an effect.
    const [openPath, setOpenPath] = useState<string | null>(null);
    const open = openPath === pathname;
    const setOpen = (o: boolean) => setOpenPath(o ? pathname : null);
    return (
        <>
            <button type="button" onClick={() => setOpen(true)} aria-label={t('CART_WITH_COUNT', {count})}
                    className="ms-1 inline-flex items-center gap-2 rounded-control border border-current px-2.5 py-1.5 transition-colors duration-(--motion-fast) hover:bg-current/15 focus-visible:bg-current/15">
                <ShoppingBagIcon aria-hidden className="size-4"/>
                <Figure value={count} className="text-sm leading-none"/>
            </button>
            <CartDrawer ctx={ctx} open={open} onOpenChange={setOpen}/>
        </>
    );
}
