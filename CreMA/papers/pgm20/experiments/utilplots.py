import numpy as np
import matplotlib.pyplot as plt
import math
import pandas as pd

def plot(data, x_column, y_column, series_column, transformation=None, ax=None, ylabel=None, xlabel=None, title=None):

    ax = ax or plt.figure(figsize=(10, 8), dpi= 80, facecolor='w', edgecolor='k').gca()

    data['method'] = data['method'].str.replace('eps','')
    
    if transformation is not None:
        data = transformation(data)
    
    data = data.replace([np.inf, -np.inf], np.nan).dropna(subset=[y_column])    
    series = np.unique(data[series_column].to_numpy())
    for s in series:       
            data_s = data.query(f"{series_column}=='{s}'")
            x = np.unique(data_s[x_column].to_numpy())
            y = data_s.groupby(x_column).mean().filter(items=[y_column]).to_numpy()

            ax.plot(x, y, marker='o', label=s)
            ax.legend()

    ax.set_ylabel(ylabel or y_column)
    ax.set_xlabel(xlabel or x_column)

    ax.set_title(title or "")
    
    return ax


### transformations ###
def compute_mean_size(data):
    dataout = data.copy()
    dataout[f"mean_size"] = data.apply(lambda t : np.nanmean([t[k.replace("lower","upper")] - t[k]  
                                                        for k in t.to_dict().keys() if "lowerbound" in k]), axis=1)
    return dataout


def get_rmse_bounds(data):

    joincolumns = [v for v in data.columns if "time" not in v and "bound" not in v and "method" not in v][1:]
    # add the corresponding exact result to each method
    df = pd.merge(
        data.query("method!='CVE'"),
        data.query("method=='CCVE'"),#.filter([v for v in data.columns if v not in ["method", "time", "query_time"]]),
        on = joincolumns,
    )
    def rmse_bounds(t):
        if np.isnan(t["lowerbound0_y"]):
            return float("nan")

        errlow = [math.pow(t[k.replace("_x","_y")]- t[k],2) for k in t.to_dict().keys() if "lowerbound" in k]
        errupp = [math.pow(t[k.replace("_x","_y")]- t[k],2) for k in t.to_dict().keys() if "upperbound" in k]
        return np.sqrt(np.nansum(errlow+errupp))

    dataout = data.copy()
    df["rmse"] = df.apply(rmse_bounds, axis=1)
    df = df.rename(columns={"method_x":"method"}).filter(joincolumns + ["method","rmse"])
    return pd.merge(dataout, df, on=joincolumns + ["method"], how="outer")


####

def plot_time(data, *args, **kwargs):
    return plot(data, "N", "time", "method", *args, **kwargs)

def plot_size(data, *args, **kwargs):
    return plot(data, "N", "mean_size", "method", transformation = compute_mean_size, *args, **kwargs)

def plot_rmse(data, *args, **kwargs):
    return plot(data, "N", "rmse", "method", transformation = get_rmse_bounds, *args, **kwargs)

    