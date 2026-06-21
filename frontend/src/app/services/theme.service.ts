import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
    private readonly STORAGE_KEY = 'darkMode';

    constructor() {
        this.applyStoredTheme();
    }

    get isDarkMode(): boolean {
        return document.body.classList.contains('dark-mode');
    }

    toggle(): void {
        const darkMode = !this.isDarkMode;
        this.setDarkMode(darkMode);
    }

    setDarkMode(enabled: boolean): void {
        if (enabled) {
            document.body.classList.add('dark-mode');
        } else {
            document.body.classList.remove('dark-mode');
        }
        localStorage.setItem(this.STORAGE_KEY, String(enabled));
    }

    private applyStoredTheme(): void {
        const stored = localStorage.getItem(this.STORAGE_KEY);
        if (stored === 'true') {
            document.body.classList.add('dark-mode');
        }
    }
}
