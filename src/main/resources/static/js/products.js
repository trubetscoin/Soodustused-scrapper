function clearForm() {
    document.getElementById("queryField").value = "";
    document.getElementById("searchForm").submit();
}

let sortOrder = "asc";
function toggleSort() {
    sortOrder = sortOrder === "asc" ? "desc" : "asc";
    sortItems();
}

function sortItems() {
    let itemContainer = document.getElementsByClassName("row")[0];
    let items = Array.from(itemContainer.getElementsByClassName("col-md-5 col-lg-2 mb-1 mb-lg-0 mb-3 mt-3"));

    items.sort(function (a, b) {
        let priceA = parseFloat(a.querySelector(".text-danger.mb-0").textContent);
        let priceB = parseFloat(b.querySelector(".text-danger.mb-0").textContent);

        if (sortOrder === "asc") {
            return priceA - priceB;
        } else {
            return priceB - priceA;
        }
    });

    items.forEach(function (item) {
        itemContainer.appendChild(item);
    });
}