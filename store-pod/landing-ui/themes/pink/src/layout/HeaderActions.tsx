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
import {CartDrawer} from './CartDrawer';

export function HeaderActions({ctx}: { ctx: PageContext }) {
    return (
        <>
            <LocaleSwitcher ctx={ctx}/>
            <AccountButton ctx={ctx}/>
            <CartControl ctx={ctx}/>
        </>
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
                <Button variant="ghost" size="icon" aria-label={t('CHANGE_LANGUAGE')}><GlobeIcon/></Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="hair rounded-none border-2">
                <DropdownMenuGroup>
                    {languages.map(lang => (
                        <DropdownMenuItem key={lang} className="cover-line rounded-none" disabled={lang === ctx.locale}
                                          onClick={() => router.replace(pathname, {locale: lang})}>
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
        return <Button variant="ghost" size="icon" aria-label={t('LOGIN')} onClick={login}><UserIcon/></Button>;
    }
    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon" aria-label={t('ACCOUNT')}><UserIcon/></Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="hair rounded-none border-2">
                <DropdownMenuGroup>
                    <DropdownMenuItem asChild className="cover-line rounded-none"><Link prefetch={false} href="/customer">{t('PROFILE')}</Link></DropdownMenuItem>
                    <DropdownMenuItem onClick={logout} variant="destructive" className="cover-line rounded-none">{t('LOGOUT')}</DropdownMenuItem>
                </DropdownMenuGroup>
            </DropdownMenuContent>
        </DropdownMenu>
    );
}

/** The basket rides the masthead as a flooded tab with its count printed in tabular figures. */
function CartControl({ctx}: { ctx: PageContext }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const pathname = usePathname();
    const {count} = useCart(ctx.storeContext);
    const [openPath, setOpenPath] = useState<string | null>(null);
    const open = openPath === pathname;
    const setOpen = (o: boolean) => setOpenPath(o ? pathname : null);
    return (
        <>
            <button type="button" onClick={() => setOpen(true)} aria-label={t('CART_WITH_COUNT', {count})}
                    className="flood cover-line ms-1 inline-flex h-9 items-center gap-2 px-3 transition-[filter] duration-(--motion-fast) hover:brightness-105">
                <ShoppingBagIcon className="size-4" aria-hidden/>
                <span className="figure text-sm">{count}</span>
            </button>
            <CartDrawer ctx={ctx} open={open} onOpenChange={setOpen}/>
        </>
    );
}
