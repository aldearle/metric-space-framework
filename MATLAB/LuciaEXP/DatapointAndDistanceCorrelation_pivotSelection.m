close all
clear all
%%
nasa = false;%
%%
traslate=true;
%%
selectionMethods= {'kmedoid',};%,,'randomData','fft','kmeans',
path='fig/';
figname ='colors' %'nasa';%'MirFlickr';%

maxnobj=5000; %max num objects to be plotted; %SAMPLE SIZE
npairs='5000';
use_RIG_RetClass=false;
%%%
 grayVal = 0.55;
 lineColor = [grayVal grayVal grayVal];
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
    linesWidth = 2;
      
else
    legendFontSize = 18;
    axisLabelFontSize = 16;
    titleFontSize = 18;
    ticksFontSize = 16;
    pivotPointsTextSize = ticksFontSize*0.7;
    labelFontWeight = 'bold';
    circleLineWidth = 0.5;
    pivotSize = 40;
    discSize = 10;
    linesWidth = 1.2;
end
%%

  
%nasa
%'colors'
%if nasa
%     figname = 'nasa';
%     n=36135;
% end

random_int_inizialize=true;

for i1=1:length(selectionMethods)
    med=selectionMethods{i1};
    textmed=med;
    switch med
        case 'randomData'
            textmed='random data points';
        case 'kmeans'
            textmed='k-means';
        case 'kmedoid'
            textmed='k-medoid';
        case 'fft'
            textmed='FFT';
    end
    
    
    filename=strcat(figname,'_',med,'-result.txt');
    dataPoints = importdata(filename);
    filenamePivot=strcat(figname,'_',med,'-refs.txt');
    pivots = importdata(filenamePivot);
    pDist=norm(pivots(1,:)-pivots(2,:));
    
    if(random_int_inizialize)
        random_int=randperm(size(dataPoints,1),maxnobj);
        random_int_inizialize=false
    end
    
    t=0;
    if(traslate)
        t=pDist/2;
    end
    figname2=figname;
    if(strcmp(figname,'colors') || strcmp(figname,'nasa'))
        figname2=strcat('SISAP {}',figname);
    end
    
    h(i1)=figure;
    hold on
    %TITLE AND LABEL
    title_text ={figname2,strcat('ref. selection:{ }',textmed)};
    title(title_text, 'FontSize', titleFontSize, 'FontWeight', labelFontWeight);
    xlabel('X', 'FontSize', axisLabelFontSize, 'FontWeight', labelFontWeight);
    ylabel('altitude from line (p_1,p_2)', 'FontSize', axisLabelFontSize, 'FontWeight', labelFontWeight);
    whitespace = 1.1;
%     yAxisLimit = max(dataPoints(2,:))*whitespace;
%     xAxisMin = min(dataPoints(1,:)-t)*whitespace;
%     xAxisMax =  max(dataPoints(1,:)-t)*whitespace;
%     axis([xAxisMin,xAxisMax,0,yAxisLimit]);
    axis equal
    set(gca, 'FontSize', ticksFontSize);
    hold on;
    %plot pivots

    pivotsHandler = scatter([-t,pDist-t],[0,0],pivotSize,[0,0,0], 'filled');
    text(-t,0.005, 0, 'p_1', 'HorizontalAlignment', 'center', 'VerticalAlignment', 'bottom', 'FontSize', pivotPointsTextSize);
    text(pDist-t, 0.005,'p_2', 'HorizontalAlignment', 'center', 'VerticalAlignment', 'bottom', 'FontSize', pivotPointsTextSize);
    
    
    %plot data points
    scatter(dataPoints(random_int,1)-t,dataPoints(random_int,2),discSize,[0,0,0],'LineWidth', circleLineWidth)
    hold off
    
    %%
    
    filenamePair=strcat(figname,'_', med,'-npairs_',npairs,'_dist.txt');
    table = readtable(strcat(filenamePair));
    
    
    h(i1+length(selectionMethods))=figure;
    %TITLE AND LABEL
    title_text ={figname2,strcat('ref. selection:{ }',textmed)};%strcat(figname2,'{ }-{ }','Distance Correlation')
    title(title_text, 'FontSize', titleFontSize, 'FontWeight', labelFontWeight);
    xlabel('original space distance', 'FontSize', axisLabelFontSize, 'FontWeight', labelFontWeight);
    ylabel('projected space distance', 'FontSize', axisLabelFontSize, 'FontWeight', labelFontWeight);
    whitespace = 1.1;
    %xmax=max(max(table.actualDist), max(table.x2simplex));
    % axis equal
    
    maxdist=max(max(table.actualDist),max(table.x2simplex));
    axis([0,maxdist,0,maxdist]);
    set(gca, 'FontSize', ticksFontSize);
    hold on;
    
    random=scatter(table.actualDist,table.x2simplex,discSize,[0,0,0],'filled','LineWidth', circleLineWidth);%
    
    xyline = linspace(0,maxdist,1000);
    plot(xyline,xyline, 'Color', lineColor, 'LineWidth', linesWidth); %
    
    if(strcmp(figname,'MirFlickr'))
        filenamePairDuplicate=strcat(figname,'_', med,'-DUPLICATEnpairs_all','_dist.txt');
        tableD = readtable(strcat(filenamePairDuplicate));
        duplicate=scatter(tableD.actualDist,tableD.x2simplex,discSize,[0,0,0],'filled','MarkerFaceColor', lineColor*0.7,'MarkerEdgeColor', lineColor*0.5,'LineWidth', circleLineWidth);
        LEG =legend([random,duplicate],'random data pairs','duplicate pairs','Location', 'northwest');%'FontSize', legendFontSize, 'Color', 'none',
        set(LEG, 'Color', 'w');
        M = findobj(LEG,'type','patch'); % Find objects of type 'patch'
        nearDuplicate=size(tableD.actualDist)
    end
    hold off;
    
    tit1=num2str(strcat(figname,'-',med));
    tit2=num2str(strcat('DIST_',figname,'-',med));
    tit1(ismember(tit1,' ,.:;!{}')) = [];
    tit2(ismember(tit2,' ,.:;!{}')) = [];
    saveas(h(i1), strcat(path,tit1,'.pdf'));
    saveas(h(i1+length(selectionMethods)),  strcat(path,tit2,'.pdf'));
    saveas(h(i1), strcat(path,tit1,'.png'));
    saveas(h(i1+length(selectionMethods)),  strcat(path,tit2,'.png'));
end


