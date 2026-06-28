import React, { useState, useRef, useEffect } from 'react';
import { useTheme } from 'shared/context/ThemeContext';
import { useLocale } from 'shared/context/LocaleContext';
import { Button } from 'shared/components/ui/button';
import { Sun, Moon, Check, ChevronDown } from 'lucide-react';
import { cn } from 'lib/utils';

const USFlag = () => (
  <svg
    width="24"
    height="18"
    viewBox="0 0 28 20"
    xmlns="http://www.w3.org/2000/svg"
  >
    <rect width="28" height="20" rx="2" fill="white" />
    <path
      fill="#D02F44"
      d="M28 0H0V1.33H28V0ZM28 2.66H0V4H28V2.66ZM0 5.33H28V6.66H0V5.33ZM28 8H0V9.33H28V8ZM0 10.66H28V12H0V10.66ZM28 13.33H0V14.66H28V13.33ZM0 16H28V17.33H0V16ZM28 18.66H0V20H28V18.66Z"
    />
    <rect width="12" height="9.33" fill="#46467F" />
    <circle cx="2" cy="2" r="0.5" fill="white" />
    <circle cx="4" cy="2" r="0.5" fill="white" />
    <circle cx="6" cy="2" r="0.5" fill="white" />
    <circle cx="8" cy="2" r="0.5" fill="white" />
    <circle cx="10" cy="2" r="0.5" fill="white" />
    <circle cx="3" cy="4" r="0.5" fill="white" />
    <circle cx="5" cy="4" r="0.5" fill="white" />
    <circle cx="7" cy="4" r="0.5" fill="white" />
    <circle cx="9" cy="4" r="0.5" fill="white" />
    <circle cx="2" cy="6" r="0.5" fill="white" />
    <circle cx="4" cy="6" r="0.5" fill="white" />
    <circle cx="6" cy="6" r="0.5" fill="white" />
    <circle cx="8" cy="6" r="0.5" fill="white" />
    <circle cx="10" cy="6" r="0.5" fill="white" />
  </svg>
);

const KhmerFlag = () => (
  <svg
    width="24"
    height="18"
    viewBox="0 0 28 20"
    xmlns="http://www.w3.org/2000/svg"
  >
    <rect width="28" height="20" rx="2" fill="#032EA1" />
    <rect y="5.5" width="28" height="9" fill="#E01E24" />
    <g transform="translate(8, 6) scale(0.35)">
      <path fill="#FFF" d="M27.021 22.897v-.902h-.542v-.901h-.496v-.586h-.226v-.451h-.438l-.238-.341v-1.599l.271-.271v-1.488l-.226.203v-.474h-.181v.226h-.359v-.723l-.429.384l.136-.485l-.249-1.116h-.136s-.111-.474-.337-.474c0 0 .09-.292-.091-.292s-.136.225-.136.225s-.315.136-.315.473l-.18-.022l-.271 1.307l.191.384l-.44-.384v.993H19.94v-.902l-.136.135v.316h-.315v-.316l.226-.203v-.428l-.182.191l-.271-.372v-.316l-.157.157l-.046-.27l.226-.36l-.034-.293l-.258.315v-.27l.113-.248l-.519-1.309l-.124-.362l-.191-.022v-.181s-.136-.315-.316-.315s-.315.315-.315.315v.181l-.191.022l-.125.362l-.518 1.309l.113.248v.27l-.259-.315l-.035.293l.226.36l-.044.27l-.158-.157v.316l-.27.372l-.181-.191v.428l.226.203v.316h-.315v-.316l-.136-.135v.902H13.58v-.993l-.44.384l.191-.384l-.271-1.307l-.181.022c0-.337-.315-.473-.315-.473s.045-.225-.136-.225c-.18 0-.09.292-.09.292c-.226 0-.338.474-.338.474h-.135l-.248 1.116l.135.485l-.428-.384v.722h-.361v-.226h-.181v.474l-.225-.203v1.488l.27.271v1.599l-.239.341h-.348v.451h-.314v.586h-.452v.901h-.495v.902h-.497l-.045.991h19.035l.045-.991h-.496z" />
    </g>
  </svg>
);

export const TopBar = () => {
  const { isDark, toggleTheme } = useTheme();
  const { locale, setLocale } = useLocale();
  const [isLangDropdownOpen, setIsLangDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);

  const languages = [
    { code: 'en', name: 'English', flag: <USFlag /> },
    { code: 'km', name: 'ខ្មែរ', flag: <KhmerFlag /> }
  ];

  const currentLanguage = languages.find(lang => lang.code === locale);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsLangDropdownOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLanguageSelect = (code) => {
    setLocale(code);
    setIsLangDropdownOpen(false);
  };

  return (
    <div className="h-[60px] bg-background border-b flex items-center justify-between px-4 sm:px-6 lg:px-8 shadow-sm sticky top-0 z-[100]">
      {/* Spacer for mobile menu button */}
      <div className="lg:hidden w-12" />
      <div className="hidden lg:block flex-1" />
      
      <div className="flex items-center gap-3 sm:gap-5">
        {/* Language Dropdown */}
        <div className="relative" ref={dropdownRef}>
          <Button
            variant="outline"
            className="flex items-center gap-1.5 sm:gap-2.5 min-w-[100px] sm:min-w-[140px] text-xs sm:text-sm"
            onClick={() => setIsLangDropdownOpen(!isLangDropdownOpen)}
          >
            <div className="flex items-center flex-shrink-0 scale-75 sm:scale-100">{currentLanguage?.flag}</div>
            <span className="hidden sm:inline flex-1 text-left">{currentLanguage?.name}</span>
            <span className="sm:hidden flex-1 text-left">{currentLanguage?.code.toUpperCase()}</span>
            <ChevronDown className={cn(
              "h-3 w-3 sm:h-3.5 sm:w-3.5 transition-transform",
              isLangDropdownOpen && "rotate-180"
            )} />
          </Button>
          
          {isLangDropdownOpen && (
            <div className="absolute top-[calc(100%+8px)] right-0 bg-background border rounded-lg shadow-lg min-w-[140px] sm:min-w-[160px] overflow-hidden z-[1000]">
              {languages.map(lang => (
                <button
                  key={lang.code}
                  className={cn(
                    "flex items-center gap-2 sm:gap-3 w-full px-3 sm:px-4 py-2.5 sm:py-3 text-xs sm:text-sm font-medium transition-colors hover:bg-accent",
                    locale === lang.code && "bg-primary/10 text-primary"
                  )}
                  onClick={() => handleLanguageSelect(lang.code)}
                >
                  <div className="flex items-center flex-shrink-0 scale-75 sm:scale-100">{lang.flag}</div>
                  <span className="flex-1 text-left">{lang.name}</span>
                  {locale === lang.code && <Check className="h-3 w-3 sm:h-4 sm:w-4" />}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Theme Toggle */}
        <div className="flex items-center gap-1.5 sm:gap-2.5 text-muted-foreground">
          <Sun className={cn("h-3.5 w-3.5 sm:h-4 sm:w-4 transition-opacity", isDark ? "opacity-40" : "opacity-100 text-orange-500")} />
          <label className="relative inline-block w-9 h-5 sm:w-11 sm:h-6 cursor-pointer">
            <input 
              type="checkbox" 
              checked={isDark} 
              onChange={toggleTheme}
              className="opacity-0 w-0 h-0"
            />
            <span className={cn(
              "absolute inset-0 rounded-full transition-colors",
              isDark ? "bg-primary" : "bg-gray-300"
            )}>
              <span className={cn(
                "absolute h-[14px] w-[14px] sm:h-[18px] sm:w-[18px] left-[3px] bottom-[3px] bg-white rounded-full transition-transform shadow-sm",
                isDark && "translate-x-4 sm:translate-x-5"
              )} />
            </span>
          </label>
          <Moon className={cn("h-3.5 w-3.5 sm:h-4 sm:w-4 transition-opacity", isDark ? "opacity-100 text-yellow-400" : "opacity-40")} />
        </div>
      </div>
    </div>
  );
};

export default TopBar;
