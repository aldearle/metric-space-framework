
nasa = true;

nasaAverages = [[0.120,	547,	0.19125,	314,	0.10475,	118,	0.085]
    [0.285,	2156,	0.943083333,	1326,	0.633166667,	837,	0.348]
    [0.530,	6278,	3.951090909,	5490,	3.476454545,	4673,	2.398]];

nasaMHT_hil = [0.12,	118,	0.085,0.285,	837,	0.348,0.53,	4673,	2.398];

colorsAverages = [[0.052,	4429,	20.119,	2357,	9.83,1860,	7.843,	2782, 12.896]
    [0.083,	9711,	43.239,	6346,	28.423,5748,	24.338, 7239,	32.593]
    [0.131,	20600,	96.423,	17250,	80.725,16227	70.09,	17808,	76.564]];

colorsMHT = [0.051768,	1860,	7.843,0.082514,	5748,	24.338,0.131163,	16227	70.09];
colorsMHT_hyp = [0.051768, 2782, 12.896, 0.082514,	7239,	32.593,0.131163,	17808,	76.564];


xaxis = [1,2,3];

h = figure;
hold on;

figname = '"colors"';
data = colorsAverages;
if nasa
    figname = '"nasa"';
    data = nasaAverages;
end

title(strcat('SISAP {}',figname, ' data set'),'FontSize',20);
xlabel('Threshold','FontSize',16);
ylabel('no. of distance calculations','FontSize',16)

axes = gca;
axes.XTick = [1,2,3];
% axes.YTick = [1,2,4,8,16,32,64];
% axes.XTickLabel = {'t_1','t_2','t_3'};
axes.XTickLabel = data(:,1);
set(gca,'FontSize',14);

plot(xaxis,data(:,2),'--','Color',[0,0,0]);
plot(xaxis,data(:,4),'-','Color',[0,0,0]);

legend({'DiSAT / Hyperbolic exclusion', 'DiSAT / Hilbert exclusion'},'FontSize',14,'Location','northwest');


L = ylim;
L(1) = 0;
ylim(L);

% num2str(get(gca,'YTick')));

labs = get(gca,'YTick');
labs = num2str(labs');
set(gca,'YTickLabel', labs);

filename = 'colors';
if nasa
    filename = 'nasa';
end

saveas(h,strcat('sisap_',filename,'_sat.pdf'));

hold off;