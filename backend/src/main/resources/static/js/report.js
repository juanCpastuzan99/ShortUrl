(() => {
    const API_URL = '/api/links';

    async function loadReport() {
        const loadingMsg = document.getElementById('loadingMsg');
        const emptyMsg = document.getElementById('emptyMsg');
        const tableWrapper = document.getElementById('tableWrapper');
        const reportBody = document.getElementById('reportBody');
        const totalLinks = document.getElementById('totalLinks');
        const totalImages = document.getElementById('totalImages');

        loadingMsg.classList.remove('d-none');
        emptyMsg.classList.add('d-none');
        tableWrapper.classList.add('d-none');

        try {
            const resp = await fetch(API_URL);
            if (!resp.ok) throw new Error('Error al cargar el reporte');
            const links = await resp.json();

            loadingMsg.classList.add('d-none');

            if (!links || links.length === 0) {
                emptyMsg.classList.remove('d-none');
                return;
            }

            totalLinks.textContent = links.length;
            totalImages.textContent = links.filter(l => l.imageUrl).length;

            reportBody.innerHTML = '';
            links.forEach((link, idx) => {
                reportBody.appendChild(buildRow(link, idx + 1));
            });

            tableWrapper.classList.remove('d-none');
        } catch (err) {
            loadingMsg.innerHTML = `<div class="alert alert-danger"><i class="bi bi-exclamation-triangle"></i> ${err.message}</div>`;
        }
    }

    function buildRow(link, index) {
        const tr = document.createElement('tr');
        const date = link.createdAt
            ? new Date(link.createdAt).toLocaleString('es-CO', { dateStyle: 'short', timeStyle: 'short' })
            : 'N/A';

        tr.innerHTML = `
            <td><span class="badge bg-secondary">${index}</span></td>
            <td>
                ${link.imageUrl
                    ? `<img src="${escHtml(link.imageUrl)}" alt="img" class="table-img" onerror="this.style.display='none'">`
                    : '<span class="text-secondary"><i class="bi bi-image"></i></span>'}
            </td>
            <td class="url-cell" title="${escHtml(link.originalUrl)}">
                <a href="${escHtml(link.originalUrl)}" target="_blank" class="link-info text-decoration-none">
                    ${escHtml(truncate(link.originalUrl, 45))}
                </a>
            </td>
            <td>
                <a href="${escHtml(link.shortUrl)}" target="_blank" class="link-warning text-decoration-none fw-bold">
                    ${escHtml(link.shortUrl)}
                </a>
            </td>
            <td class="desc-cell text-secondary" title="${escHtml(link.description || '')}">
                ${escHtml(truncate(link.description || 'Sin descripción', 60))}
            </td>
            <td><small class="text-secondary">${date}</small></td>
        `;
        return tr;
    }

    function escHtml(str) {
        return String(str)
            .replace(/&/g, '&amp;').replace(/</g, '&lt;')
            .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    function truncate(str, max) {
        return str.length > max ? str.slice(0, max) + '…' : str;
    }

    window.loadReport = loadReport;
    loadReport();
})();
