import 'server-only';
import {cache} from 'react';
import {extractSsrContext} from '@store-front/services/store-context-ssr-utils';
import {StoreService} from '@store-front/services/store-service';
import type {Store, StoreContext} from '@store-front/types';

/** Per-request memoised store context (Store-Id header + locale + gateways). */
export const getStoreContext = cache(async (): Promise<StoreContext> => extractSsrContext());

/** Per-request memoised store record — layout, pages and metadata all share one fetch. */
export const getStore = cache(async (): Promise<Store> => StoreService.getStore(await getStoreContext()));
