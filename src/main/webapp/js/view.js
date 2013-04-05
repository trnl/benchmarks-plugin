var color = d3.scale.category10(), margin = 40, w = 900, h = 150;
var fieldsToVisualize;

function drawChart(panel, result, fieldName) {
    var t = panel.append("svg:svg")
        .attr("height", h)
//        .attr("width", w + margin * 2)
        .attr("width", "100%")
        .style("border-bottom", fieldName != fieldsToVisualize[fieldsToVisualize.length - 1] ? "1px solid #BBB" : "")
        .append("g")
        .attr("transform", "translate(" + margin * 1.5 + "," + margin * 0.5 + ")");

    var e = result.values.map(function (g) {
        return g.build
    });
    var s = d3.max(result.values, function (g) {
        return g[fieldName];
    });
    var q = d3.min(result.values, function (g) {
        return g[fieldName];
    });
    var n = d3.scale.ordinal().domain(e).rangePoints([0, w]);
    var m = d3.scale.linear().range([h, 0]).domain([q, s]);
    var a = d3.svg.axis().scale(m).orient("left").tickSize(-w).tickPadding(15).tickFormat(function (g) {
        return d3.format(".2s")(g);
    });
    var l = d3.svg.axis().scale(n).tickSize(h).tickPadding(15);
    var p = t.append("svg:g").attr("class", "yTick").call(a);
    var b = t.append("svg:g").attr("class", "xTick").call(l);
    b.selectAll("text").attr("transform",function (g) {
        return"rotate(-90 " + 0 + " " + d3.select(this).attr("y") + "),translate(0, -4)"
    }).attr("text-anchor", "end");
    var o = t.append("svg:g").attr("class", "line").attr("key", result.key);
    var j = d3.svg.line().x(function (g) {
        return n(g.build)
    }).y(function (g) {
            return m(g[fieldName])
        }).interpolate("linear");
    o.selectAll(".data-line").data([result.values]).enter().append("path").attr("class", "data-line").style("stroke", color(result.key)).attr("d", j(result.values));
    o.selectAll(".data-point").data(result.values).enter()
        .append("svg:circle")
        .attr("class", "data-point")
        .attr("tooltip", function (d) {
            var text = "";

            text += "<h3>" + d.build + "</h3>";

            text += "<table>";
            text += "<tr class=main><th>Ops User</th><td>" + d.opsUser.toFixed(12) + "</td></tr>";
            text += "<tr><th>Ops Real</th><td>" + d.opsReal.toFixed(12) + "</td></tr>";
            text += "<tr><th>Time User</th><td>" + d.timeUser.toFixed(6) + "</td></tr>";
            text += "<tr><th>Time Real</th><td>" + d.timeReal.toFixed(6) + "</td></tr>";
            text += "<tr><th>Iterations</th><td>" + d.iterations+ "</td></tr>";
            text += "<tr><th>Rounds</th><td>" + d.rounds+ "</td></tr>";
            text += "</table>";

            if ( d.change ) {
                text += "<table>";
                text += "<tr><th>Commit</th><td>" + d.change.id+ "</td></tr>";
                text += "<tr><th>Date</th><td>" + d.change.date + "</td></tr>";
                text += "<tr><th>Author</th><td>" + d.change.author + "</td></tr>";
                text += "<tr><th>E-Mail</th><td>" + d.change.email + "</td></tr>";
                text += "</table>";
            }

            return text;
        })
        .attr("cx",function (g) {return n(g.build)})
        .style("stroke", color(result.key)).attr("cy",function (g) {
            return m(g[fieldName])
        })
        .attr("r", 4).on("mouseover",function (g) {
            d3.select(this).transition().style("fill", color(g.title));
        }).on("mouseout", function () {
            d3.select(this).transition().style("fill", "#fff")
        });
    var c = t.append("svg:g").attr("class", "legend").attr("transform", "translate(0," + (b.node().getBoundingClientRect().height + margin / 2) + ")");
    var d = c.selectAll(".legend-entry").data([fieldName]).enter().append("svg:g").attr("class", "legend-entry");
    d.append("line").attr("x1", 0).attr("y1", 0).attr("x2", 15).attr("y2", 0).style("stroke-width", 2).style("stroke", color(result.key));
    d.append("text").text(function (g) {
        return g
    }).attr("text-anchor", "start").attr("dy", ".32em").attr("dx", "17");
    var k = 0, i = 0;
    d.attr("transform", function (v) {
        var u = d3.select(this).select("text").node().getComputedTextLength() + 30;
        if (w < i + u) {
            i = 0;
            k += 30
        }
        var g = "translate(" + i + "," + k + ")";
        i += u;
        return g
    });
    t.select(function () {
        return this.parentNode
    }).attr("height", b.node().getBoundingClientRect().height + c.node().getBoundingClientRect().height + margin + 10)
}

function extractName(b) {
    return b.key.substring(b.key.lastIndexOf(".") + 1, b.key.length);
}
function drawCharts(b) {
    var panel = d3.select("#main-panel").append("div").attr("class", "report");
    panel.append("h2")
        .text(extractName(b))
        .append('a')
        .attr('href', '#')
        .attr('id', extractName(b))
        .attr('class', 'jump-to')
        .text('↑');
    panel.classed("chart");
    fieldsToVisualize.forEach(function (field) {
        drawChart(panel, b, field);
    });
}
d3.json("getReport?key=" + d3.select(".report-title").text(), function (data) {
    fieldsToVisualize = data.fieldsToVisualize;
    var benchmarks = d3.merge(data.report.map(function (build) {
        build.benchmarks.forEach(function (benchmark) {
            benchmark.build = build.build;
            benchmark.change = build.change;
        });
        return build.benchmarks;
    }));
    var a = d3.nest().key(function (bm) {
        return bm.title
    }).entries(benchmarks);
    d3.select("#main-panel").style("height", a.length * h + "px");
    var list = d3.select("#main-panel").append("ul").attr("class", "report-list");
    a.forEach(function (d) {
        var name = extractName(d);
        list
            .append('li')
            .append('a')
            .attr('href', '#' + name)
            .text(name);

        drawCharts(d)
    })
});