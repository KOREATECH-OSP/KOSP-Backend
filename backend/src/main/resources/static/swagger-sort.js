window.addEventListener('load', function () {
    function getSortKey(summary) {
        if (!summary) return Infinity;
        var match = summary.match(/^(\d+)\./);
        return match ? parseInt(match[1], 10) : Infinity;
    }

    function sortOperations() {
        var blocks = document.querySelectorAll('.opblock-tag-section');
        blocks.forEach(function (section) {
            var container = section.querySelector('.operation-tag-content');
            if (!container) return;

            var items = Array.from(container.children);
            var sorted = items.slice().sort(function (a, b) {
                var summaryA = a.querySelector('.opblock-summary-description');
                var summaryB = b.querySelector('.opblock-summary-description');
                var textA = summaryA ? summaryA.textContent.trim() : '';
                var textB = summaryB ? summaryB.textContent.trim() : '';
                var keyA = getSortKey(textA);
                var keyB = getSortKey(textB);
                if (keyA === Infinity && keyB === Infinity) return textA.localeCompare(textB);
                return keyA - keyB;
            });

            var changed = sorted.some(function (el, i) { return el !== items[i]; });
            if (!changed) return;
            sorted.forEach(function (el) { container.appendChild(el); });
        });
    }

    var observer = new MutationObserver(function () { sortOperations(); });
    observer.observe(document.body, { childList: true, subtree: true });

    setTimeout(sortOperations, 500);
    setTimeout(sortOperations, 1500);
});
