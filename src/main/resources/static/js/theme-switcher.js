document.addEventListener("DOMContentLoaded", function() {
    function getCookie(name) {
        let value = "; " + document.cookie;
        let parts = value.split("; " + name + "=");
        if (parts.length === 2) return parts.pop().split(";").shift();
    }

    const currentTheme = getCookie('theme') || 'dark';
    document.body.classList.add(`theme-${currentTheme}`);

    document.querySelectorAll('.theme-option').forEach(function(element) {
        element.addEventListener('click', function(e) {
            e.preventDefault();
            const selectedTheme = this.getAttribute('data-theme');

            document.body.classList.forEach(function(cls) {
                if (cls.startsWith('theme-')) {
                    document.body.classList.remove(cls);
                }
            });

            document.body.classList.add(`theme-${selectedTheme}`);

            fetch(`/theme?theme=${selectedTheme}`, {
                method: 'GET',
                credentials: 'same-origin'
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Failed to set theme on server');
                    }
                })
                .catch(error => {
                    console.error('Error setting theme:', error);
                });
        });
    });
});
