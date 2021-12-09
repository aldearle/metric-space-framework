
nasa = true;

nasaDists = [
    [0.120,0.285,0.530] %thresholds
    [547,2156,6278]     %DiSAT
    [314,1326,5490]     %DiSAT - Hilbert
    [156.37758,1091.2732,5422.4355] %HilMonPT
    [259.68268,1622.0414,6325.383]  %HypMonPT
];

nasaDistsForVHpaperLegend = {'BalPT','PT','Bal-MonPT','MonPT','Leanest Tree','Leanest Tree/LAESA','adjusted L/L cost'};
nasaDistsForVHpaper = [
    [0.120,0.285,0.530] %thresholds
    [547,2156,6278]     %DiSAT
    [259.68268,1622.0414,6325.383]  %HypMonPT
    [328.4304,2304.079,8049.2183]   %BalHypMonPT	
    [564.8986,3167.3616,11043.503]  %HypPT	
    [764.38403,4651.366,13379.09]   %BalHypPT
    [558.136,3957.0173,15929.345]   %permutation tree
    [281,2931,11861]   %leanest tree
    [20,308,4457]   %leanest tree with 3p filter
    [260.8,	2899.2,	17511.4] %true L/L cost

];

colorsDistsForVHpaper = [
    [0.052,0.083,0.131] %thresholds
    [4429,9711,20600]     %DiSAT
    [2926.2246,7623.076,18988.402]  %HypMonPT
	[4917.951,	12082.884,	25980.014]  %BalHypMonPT
    [4768.594,	11911.283,	28493.99]   %HypPT	
	[6458.331,	15868.994,	33762.652]  %BalHypPT
    [3516.3203, 10221.828,25799.82]   %permutation tree
    [3975, 10815,25277]   %leanest tree
    [464, 2761,12428]   %leanest tree with 3p filter
    [1103.516949,	4716.864407,	17860.07627]    %true L/L cost
];

nasaSepDists = [
    [0.120,0.285,0.530] %thresholds
    [156.37758,1091.2732,5422.4355] %HilFar
    [239.402,2553.1345,10787.527]  %HilMed
    [298.83386,2853.758,11132.65]  %HilNear
    [259.68268,1622.0414,6325.383]  %HypFar
    [1905.5303,7125.943,13674.983]  %HypMed
    [4874.5015, 8378.061,14116.57]  %HypNear
];

colorsDists = [
    [0.052,0.083,0.131] %thresholds
    [4429,9711,20600]     %DiSAT
    [2357,6346,17250]     %DiSAT - Hilbert
    [1941.0485,5962.2266,16995.316] %HilMonPT
    [2926.2246,7623.076,18988.402]  %HypMonPT
];

colorsSepDists = [
    [0.052,0.083,0.131] %thresholds
    [1941.0485,5962.2266,16995.316] %HilFar
    [2465.0178,7943.365,21727.06]  %HilMed
    [3993.191,12222.745,29423.488]  %HilNear
    [2926.2246,7623.076,18988.402]  %HypFar
    [8987.546,16362.817,27974.031]  %HypMed
    [21850.48,28843.021,40668.57]  %HypNear
];

colorsBalDists = [
    [0.052,0.083,0.131] %thresholds
    [1941.0485,5962.2266,16995.316] %HilMonPT
    [2926.2246,7623.076,18988.402]  %HypMonPT
    [3973.2664,	10918.162,25582.904] %BalHilVMonPT
    [4917.951,12082.884,25980.014] %BalHypMonPT
    [5077.543,13164.956,29099.559] %BalHilHMonPT
];

colorsHilHBalance = [
    [0.052,0.083,0.131] %thresholds
    [4170.6963,	12161.796,	28568.99]%BalHilVMonPT
    [3723.2407,	10525.613,	25167.25]%BalHilHMonPT
    [3993.191,	12222.745,	29423.488]%HilMonPT
];

colorsBalTimes = [
    [0.052,0.083,0.131] %thresholds
    [1941.0485,5962.2266,16995.316] %HilMonPT
    [2926.2246,7623.076,18988.402]  %HypMonPT
    [3973.2664,	10918.162,25582.904] %BalHilVMonPT
    [8508.659,16571.98,30142.754] %BalHypMonPT
    [5077.543,13164.956,29099.559] %BalHilHMonPT
];


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
data = colorsDistsForVHpaper;
if nasa
    figname = '"nasa"';
    data = nasaDistsForVHpaper;
end

title(strcat('SISAP {}',figname, ' data set'),'FontSize',20);
xlabel('Threshold','FontSize',16);
ylabel('no. of distance calculations','FontSize',16)

axes = gca;
axes.XTick = [1,2,3];
% axes.YTick = [1,2,4,8,16,32,64];
% axes.XTickLabel = {'t_1','t_2','t_3'};
axes.XTickLabel = data(1,:);
set(gca,'FontSize',14);

% plot(xaxis,data(2,:),'--','Color',[0,0,0]);
plot(xaxis,data(6,:),':','Color',[0,0,0]);
plot(xaxis,data(5,:),':','Color',[0,0,0]);
plot(xaxis,data(4,:),':','Color',[0,0,0]);
plot(xaxis,data(3,:),':.','Color',[0,0,0]);
plot(xaxis,data(8,:),'-.','Color',[0,0,0]);
plot(xaxis,data(9,:),'--.','Color',[0,0,0]);
plot(xaxis,data(10,:),'-','Color',[0,0,0]);

legend(nasaDistsForVHpaperLegend,'FontSize',14,'Location','northwest');


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