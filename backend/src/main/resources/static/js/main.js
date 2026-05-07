(() => {
    const API_URL = '/api/links';

    const form = document.getElementById('shortenForm');
    const originalUrlInput = document.getElementById('originalUrl');
    const imageUrlInput = document.getElementById('imageUrl');
    const descriptionInput = document.getElementById('description');
    const submitBtn = document.getElementById('submitBtn');
    const btnText = document.getElementById('btnText');
    const btnLoader = document.getElementById('btnLoader');
    const copyBtn = document.getElementById('copyBtn');
    const shortUrlBanner = document.getElementById('shortUrlBanner');
    const shortUrlLink = document.getElementById('shortUrlLink');
    const charCount = document.getElementById('charCount');
    const imagePreview = document.getElementById('imagePreview');
    const previewImg = document.getElementById('previewImg');

    const urlRegex = /^(https?|ftp):\/\/[^\s/$.?#].[^\s]*$/i;

    descriptionInput.addEventListener('input', () => {
        charCount.textContent = descriptionInput.value.length;
    });

    imageUrlInput.addEventListener('blur', () => {
        const val = imageUrlInput.value.trim();
        if (urlRegex.test(val)) {
            previewImg.src = val;
            previewImg.onload = () => imagePreview.classList.remove('d-none');
            previewImg.onerror = () => imagePreview.classList.add('d-none');
        } else {
            imagePreview.classList.add('d-none');
        }
    });

    originalUrlInput.addEventListener('blur', () => validateUrl(originalUrlInput, 'urlError'));
    imageUrlInput.addEventListener('blur', () => validateUrl(imageUrlInput, 'imageError'));
    descriptionInput.addEventListener('blur', () => validateDescription());

    function validateUrl(input, errorId) {
        const val = input.value.trim();
        const errorEl = document.getElementById(errorId);
        if (!val) return setError(input, errorEl, 'Este campo es obligatorio.');
        if (!urlRegex.test(val)) return setError(input, errorEl, 'URL inválida (debe iniciar con http:// o https://).');
        return clearError(input, errorEl);
    }

    function validateDescription() {
        const val = descriptionInput.value.trim();
        const errorEl = document.getElementById('descError');
        if (!val) return setError(descriptionInput, errorEl, 'La descripción es obligatoria.');
        const wordCount = val.split(/\s+/).filter(w => w.length > 0).length;
        if (wordCount < 5) return setError(descriptionInput, errorEl, `Mínimo 5 palabras (llevas ${wordCount}).`);
        if (val.length > 500) return setError(descriptionInput, errorEl, 'Máximo 500 caracteres.');
        return clearError(descriptionInput, errorEl);
    }

    function setError(input, errorEl, msg) {
        input.classList.remove('is-valid');
        input.classList.add('is-invalid');
        errorEl.textContent = msg;
        errorEl.classList.add('text-danger');
        return false;
    }

    function clearError(input, errorEl) {
        input.classList.remove('is-invalid');
        input.classList.add('is-valid');
        errorEl.textContent = '';
        errorEl.classList.remove('text-danger');
        return true;
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const v1 = validateUrl(originalUrlInput, 'urlError');
        const v2 = validateUrl(imageUrlInput, 'imageError');
        const v3 = validateDescription();
        if (!v1 || !v2 || !v3) return;

        setLoading(true);
        shortUrlBanner.classList.add('d-none');
        copyBtn.classList.add('d-none');

        try {
            const resp = await fetch(API_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    originalUrl: originalUrlInput.value.trim(),
                    imageUrl: imageUrlInput.value.trim(),
                    description: descriptionInput.value.trim()
                })
            });

            if (!resp.ok) {
                const err = await resp.json();
                alert('Error: ' + Object.values(err).join(' | '));
                return;
            }

            const data = await resp.json();
            originalUrlInput.value = data.shortUrl;
            originalUrlInput.classList.remove('is-invalid');
            originalUrlInput.classList.add('is-valid');

            shortUrlLink.textContent = data.shortUrl;
            shortUrlLink.href = data.shortUrl;
            shortUrlBanner.classList.remove('d-none');
            shortUrlBanner.classList.add('d-flex');
            copyBtn.classList.remove('d-none');
        } catch {
            alert('Error de conexión con el servidor.');
        } finally {
            setLoading(false);
        }
    });

    copyBtn.addEventListener('click', async () => {
        const url = originalUrlInput.value;
        try {
            await navigator.clipboard.writeText(url);
        } catch {
            originalUrlInput.select();
            document.execCommand('copy');
        }
        copyBtn.innerHTML = '<i class="bi bi-check-lg"></i> Copiado';
        setTimeout(() => { copyBtn.innerHTML = '<i class="bi bi-clipboard"></i> Copiar'; }, 2000);
    });

    function setLoading(state) {
        submitBtn.disabled = state;
        btnText.classList.toggle('d-none', state);
        btnLoader.classList.toggle('d-none', !state);
    }
})();
