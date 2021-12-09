use_RIG_RetClass=false; %set false for using Franco's FIG_Rect class
if(use_RIG_RetClass)
    legendFontSize = 32;
    axisLabelFontSize = 30;
    titleFontSize = 34;
    ticksFontSize = 30;
    pivotPointsTextSize = ticksFontSize - 4;
    labelFontWeight = 'bold';
    circleLineWidth = 2;
    pivotSize = 250;
    discSize = 100;
    lineWidth = 3;
    markerSize=15;
else
    legendFontSize = 18;
    axisLabelFontSize = 16;
    titleFontSize = 18;
    ticksFontSize = 16;
    pivotPointsTextSize = ticksFontSize - 4;
    labelFontWeight = 'bold';
    circleLineWidth = 1;
    pivotSize = 125;
    discSize = 50;
    lineWidth = 2.5;
    markerSize=7;
end
%%
nasa = true;
% the following figureXX flags refer to figures in the SISAP2016 paper
figure10 = false;
figure11 = false;
figure12 = false;
figure13 = true;
%%
nasaALLDists_Hilbert = [
    [0.120,0.285,0.530] %thresholds
    [314,1326,5490]     %DiSAT - Hilbert
    %[156.37758,1091.2732,5422.4355] %Hilbert Unbalenced monotonous Tree (MonPT) %OLD RUN
    [197.05,	1547.45,	6773.1] %Hilbert Unbalenced monotonous Tree (MonPT) %new  RUN
    
];%fig10

nasaDists = [
    [0.120,0.285,0.530] %thresholds
    [547,2156,6278]     %DiSAT
    [314,1326,5490]     %DiSAT - Hilbert
    [156.37758,1091.2732,5422.4355] %HilMonPT - Hilbert (Unbalenced monotonous Tree)
    [259.68268,1622.0414,6325.383]  %HypMonPT
];%fig10

nasaSepDists = [
    [0.120,0.285,0.530] %thresholds
    [156.37758,1091.2732,5422.4355] %HilFar
    [239.402,2553.1345,10787.527]  %HilMed
    [298.83386,2853.758,11132.65]  %HilNear
    [259.68268,1622.0414,6325.383]  %HypFar
    [1905.5303,7125.943,13674.983]  %HypMed
    [4874.5015, 8378.061,14116.57]  %HypNear
];%figure12

nasaHilbertHyperplanePartitioning = [
    [0.120,0.285,0.530] %thresholds
    [204.75,	1785.8,	7834.5] % Balenced monotonous Tree [MonPT-Balanced]
        [197.05,	1547.45,	6773.1] %HilMonPT unbalaced Unbalenced monotonous Tree [MonPT-Unbalanced]
        [33.45,	309.65,	3630.7] % Balanced monotonous PCA Tree [PCA Mon_PT-Balanced]
];%fig13

%%

colorsHilbertHyperplanePartitioning = [
    [0.052,0.083,0.131] %thresholds
    [2495.35,	8215.65,	22576.8] % Balenced monotonous Tree [MonPT-Balanced]
    [2225.6,	7410.75,	21029.8] %HilMonPT unbalaced Unbalenced monotonous Tree [MonPT-Unbalanced]
      [242.1	1360.7	7745.5] % Balanced monotonous PCA Tree [MonPCA_PT-Balanced]
];%fig13

colorsDists = [
    [0.052,0.083,0.131] %thresholds
    [4429,9711,20600]     %DiSAT
    [2357,6346,17250]     %DiSAT - Hilbert
    [1941.0485,5962.2266,16995.316] %HilMonPT -Hilbert? Unbalenced monotonous Tree
    [2926.2246,7623.076,18988.402]  %HypMonPT - 
];%fig10

colorsSepDists = [
    [0.052,0.083,0.131] %thresholds
    [1941.0485,5962.2266,16995.316] %HilFar
    [2465.0178,7943.365,21727.06]  %HilMed
    [3993.191,12222.745,29423.488]  %HilNear
    [2926.2246,7623.076,18988.402]  %HypFar
    [8987.546,16362.817,27974.031]  %HypMed
    [21850.48,28843.021,40668.57]  %HypNear
];%fig 11

% colorsBalDists = [
%     [0.052,0.083,0.131] %thresholds
%     [1941.0485,5962.2266,16995.316] %HilMonPT
%     [2926.2246,7623.076,18988.402]  %HypMonPT
%     [3973.2664,	10918.162,25582.904] %BalHilVMonPT
%     [4917.951,12082.884,25980.014] %BalHypMonPT
%     [5077.543,13164.956,29099.559] %BalHilHMonPT
% ];

colorsHilHBalance = [
    [0.052,0.083,0.131] %thresholds
    [4170.6963,	12161.796,	28568.99] %BalHilVertical MonPT
    [3723.2407,	10525.613,	25167.25] %BalHilHorizontal MonPT
    [3993.191,	12222.745,	29423.488] %HilMonPT unbalaced 
];%fig12

% colorsBalTimes = [
%     [0.052,0.083,0.131] %thresholds
%     [1941.0485,5962.2266,16995.316] %HilMonPT
%     [2926.2246,7623.076,18988.402]  %HypMonPT
%     [3973.2664,	10918.162,25582.904] %BalHilVMonPT
%     [8508.659,16571.98,30142.754] %BalHypMonPT
%     [5077.543,13164.956,29099.559] %BalHilHMonPT
% ];

xaxis = [1,2,3];

h = figure(200);
clf;
hold on;


if nasa
    figname = '"nasa"';
    if(figure10)
        data = nasaDists; % paper sisap2016, supermetrics, figure 11
    elseif(figure11)
        data = nasaSepDists; % paper sisap2016, supermetrics, figure 12
    elseif(figure12)
        data = nasaSepDists; % paper sisap2016, supermetrics, figure 13
    elseif(figure13)
        data=nasaHilbertHyperplanePartitioning;
    end
else
    figname = '"colors"';
    % data = colorsDistsForVHpaper;
    
    if(figure10)
        data = colorsDists; % paper sisap2016, supermetrics, figure 11
    elseif(figure11)
        data = colorsSepDists; % paper sisap2016, supermetrics, figure 12
    elseif(figure12)
        data = colorsHilHBalance; % paper sisap2016, supermetrics, figure 13
    elseif(figure13)
        data=colorsHilbertHyperplanePartitioning;
    end
    
end

title(strcat('SISAP {}',figname, ' data set'),'FontSize', titleFontSize, 'FontWeight', labelFontWeight);
xlabel('Threshold', 'FontSize', axisLabelFontSize, 'FontWeight', labelFontWeight);
ylabel('no. of distance calculations','FontSize', axisLabelFontSize, 'FontWeight', labelFontWeight)

axes = gca;
axes.XTick = [1,2,3];
set(gca,'FontSize',14);
set(gca, 'XTick', 1:length(data(1, :)));
set(gca, 'XTickLabel', data(1, :), 'FontSize', ticksFontSize);


vhPaper = false;
if vhPaper
% plot(xaxis,data(2,:),'--','Color',[0,0,0]);
plot(xaxis,data(6,:),':','Color',[0,0,0]);
plot(xaxis,data(5,:),':','Color',[0,0,0]);
plot(xaxis,data(4,:),':','Color',[0,0,0]);
plot(xaxis,data(3,:),':.','Color',[0,0,0]);
plot(xaxis,data(8,:),'-.','Color',[0,0,0]);
plot(xaxis,data(9,:),'--.','Color',[0,0,0]);
plot(xaxis,data(10,:),'-','Color',[0,0,0]);
legend(nasaDistsForVHpaperLegend,'FontSize',14,'Location','northwest');
else
    cMap = flipud(gray(10+1));
    
    % the correct data MUST be selected before reaching this point
    if figure10
        leg1 = plot(xaxis,data(2,:),'--','Color', cMap(5, :), 'LineWidth', lineWidth); % disat hyp
        leg2 = plot(xaxis,data(3,:),'-','Color',cMap(5, :), 'LineWidth', lineWidth); % disat hil
        leg3 = plot(xaxis,data(4,:),'-','Color', cMap(11, :), 'LineWidth', lineWidth); %mon hil
        leg4 = plot(xaxis,data(5,:),'--','Color', cMap(11,:), 'LineWidth', lineWidth); % mon hyp
        if(use_RIG_RetClass)
            LEG = legend([leg1, leg2, leg4, leg3], {'DiSAT / Hyperbolic', 'DiSAT / Hilbert', 'MonPT / Hyperbolic', 'MonPT / Hilbert'}, 'Location', 'NorthWest');
            set(LEG, 'Color', 'none');
        else
            LEG =legend([leg1, leg2, leg4, leg3], {'DiSAT / Hyperbolic', 'DiSAT / Hilbert', 'MonPT / Hyperbolic', 'MonPT / Hilbert'}, 'Location', 'northwest');
            set(LEG, 'Color', 'w');
        end
    elseif figure11

        mult = 3;
        cMap = flipud(gray(25+1));
        idx = 2;        
        plot(xaxis, data(5, :), '--d', 'Color', cMap(7, :), 'LineWidth', lineWidth,  'MarkerSize',markerSize);
        plot(xaxis, data(6, :), '--s', 'Color', cMap(12, :), 'LineWidth', lineWidth,  'MarkerSize',markerSize);
        plot(xaxis, data(7, :), '--', 'Color', cMap(20, :), 'LineWidth', lineWidth);
        plot(xaxis, data(2, :), '-d', 'Color', cMap(7, :), 'LineWidth', lineWidth,  'MarkerSize',markerSize); 
        plot(xaxis, data(3, :), '-s', 'Color', cMap(12, :), 'LineWidth', lineWidth,  'MarkerSize',markerSize);
        plot(xaxis, data(4, :), '-', 'Color', cMap(20, :), 'LineWidth', lineWidth); 

        
        
        
        if(use_RIG_RetClass)
            LEG = legend({'Far / Hyperbolic', 'Med / Hyperbolic', 'Near / Hyperbolic', 'Far / Hilbert', 'Med / Hilbert', 'Near / Hilbert'},...
            'FontSize', legendFontSize-8, 'Location', 'NorthWest');
            set(LEG, 'Color', 'none');
        else
            LEG =legend({'Far / Hyperbolic', 'Med / Hyperbolic', 'Near / Hyperbolic', 'Far / Hilbert', 'Med / Hilbert', 'Near / Hilbert'},...
            'FontSize', legendFontSize-8, 'Location', 'northwest');
            set(LEG, 'Color', 'w');
        end
    elseif figure12
        cMap = flipud(gray(21));
         plot(xaxis, data(2, :), '--', 'Color', cMap(7,:), 'LineWidth', lineWidth,  'MarkerSize', markerSize);
         plot(xaxis, data(3, :), '-', 'Color', cMap(13,:), 'LineWidth', lineWidth,  'MarkerSize', markerSize);
         plot(xaxis, data(4, :), '-.', 'Color', cMap(20,:), 'LineWidth', lineWidth,  'MarkerSize', markerSize);
        
        if(use_RIG_RetClass)
            LEG =  legend('H-V: Hilbert, vertical partition', 'H-H: Hilbert, horizontal partition', 'Hilbert, unbalanced', 'FontSize', legendFontSize, 'Location', 'NorthWest');
            set(LEG, 'Color', 'none');
        else
            LEG = legend('H-V: Hilbert, vertical partition', 'H-H: Hilbert, horizontal partition', 'Hilbert, unbalanced', 'FontSize', legendFontSize, 'Location', 'northwest');
            set(LEG, 'Color', 'w');
        end
    elseif figure13
        %thresholds
      
     
 
        mult = 3;

        leg2 = plot(xaxis,data(2,:),'-','Color',cMap(5, :), 'LineWidth', lineWidth);         % [MonPT-Balanced]
        leg3 = plot(xaxis,data(3,:),'-','Color', cMap(11, :), 'LineWidth', lineWidth);       % [MonPT-Unbalanced]
        leg4 = plot(xaxis,data(4,:),'--','Color', cMap(11,:), 'LineWidth', lineWidth); % [PCA_MonPT-Balanced 
        if(use_RIG_RetClass)
            LEG = legend([leg4, leg3, leg2], {'PCA MonPT-Balanced', 'MonPT-Unbalanced', 'MonPT-Balanced'}, 'Location', 'NorthWest');
            set(LEG, 'Color', 'none');
        else
            LEG =legend([ leg4, leg3, leg2], {'PCA MonPT-Balanced', 'MonPT-Unbalanced', 'MonPT-Balanced'}, 'Location', 'northwest');
            set(LEG, 'Color', 'w');
        end
    
    end
    
  if( use_RIG_RetClass)
   FIG_Rect(200, 2, 2);
  end
end % if vhPaper

if(nasa)
    labs = 0:1000:8000;
    ylim([0 labs(end)])
else
    labs = 0:5000:30000;
    ylim([0 labs(end)])
end
set(gca, 'YTick', labs(2:end));
set(gca,'YTickLabel', labs(2:end), 'FontSize', ticksFontSize);
grid off
filename = 'colors';
if nasa
    filename = 'nasa';
end



if use_RIG_RetClass
   FIG_Save(200, strcat('sisap_',filename,'_sat_HilH'), 'jpeg', 300, 90);
else
    saveas(h,strcat('sisap_',filename,'_sat.pdf'));
end


hold off;